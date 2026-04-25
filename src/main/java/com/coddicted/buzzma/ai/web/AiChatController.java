package com.coddicted.buzzma.ai.web;

import com.coddicted.buzzma.catalog.persistence.DealsEntity;
import com.coddicted.buzzma.catalog.persistence.DealsRepository;
import com.coddicted.buzzma.identity.persistence.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiChatController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AiChatController.class);

  private static final String GEMINI_BASE =
      "https://generativelanguage.googleapis.com/v1beta/models/";
  private static final List<String> MODEL_FALLBACKS =
      List.of("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro");

  private final UsersRepository usersRepository;
  private final DealsRepository dealsRepository;
  private final ObjectMapper objectMapper;
  private final String geminiApiKey;
  private final String geminiModel;
  private final HttpClient httpClient;

  public AiChatController(
      UsersRepository usersRepository,
      DealsRepository dealsRepository,
      ObjectMapper objectMapper,
      @Value("${app.gemini.api-key:}") String geminiApiKey,
      @Value("${app.gemini.model:gemini-2.0-flash}") String geminiModel) {
    this.usersRepository = usersRepository;
    this.dealsRepository = dealsRepository;
    this.objectMapper = objectMapper;
    this.geminiApiKey = geminiApiKey;
    this.geminiModel = geminiModel;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  @PostMapping("/chat")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> chat(@RequestBody ChatRequest request, @CurrentUserId UUID actorId) {

    String rawMessage = request.message() != null ? request.message().trim() : "";
    String msg = rawMessage.toLowerCase();

    List<Map<String, Object>> products = resolveProducts(actorId, request.products());

    boolean hasProducts = !products.isEmpty();
    boolean hasOrders = request.orders() != null && !request.orders().isEmpty();
    boolean hasTickets = request.tickets() != null && !request.tickets().isEmpty();

    // ── Rule-based intent engine ─────────────────────────────────────────────

    if (hasProducts) {
      boolean wantsAll = msg.contains("all deals") || msg.contains("all available deals");
      boolean wantsLowest = msg.contains("lowest") || msg.contains("cheapest");
      boolean wantsDeals = msg.contains("deals") || msg.contains("loot");
      boolean wantsSpecific =
          msg.contains("show") || msg.contains("find") || msg.contains("show me");
      int count = extractCount(msg);

      if (wantsAll) {
        return Map.of(
            "text", "Here are all available deals right now.",
            "intent", "search_deals",
            "uiType", "product_card",
            "data", products.subList(0, Math.min(50, products.size())));
      }
      if (wantsSpecific && rawMessage.length() > 6) {
        Map<String, Object> best = findBestMatch(products, msg);
        if (best != null) {
          return Map.of(
              "text", "Here is the deal you asked for.",
              "intent", "search_deals",
              "uiType", "product_card",
              "data", List.of(best));
        }
      }
      if (wantsLowest) {
        List<Map<String, Object>> sorted = new ArrayList<>(products);
        sorted.sort((a, b) -> Double.compare(toDouble(a.get("price")), toDouble(b.get("price"))));
        int take = count > 0 ? count : 1;
        String text =
            take == 1
                ? "Here is the lowest priced deal."
                : "Here are the lowest " + take + " deals.";
        return Map.of(
            "text",
            text,
            "intent",
            "search_deals",
            "uiType",
            "product_card",
            "data",
            sorted.subList(0, Math.min(take, sorted.size())));
      }
      if (wantsDeals) {
        int take = count > 0 ? count : 5;
        return Map.of(
            "text",
            "Here are the top " + take + " deals for you.",
            "intent",
            "search_deals",
            "uiType",
            "product_card",
            "data",
            products.subList(0, Math.min(take, products.size())));
      }
    } else if (msg.contains("deal") || msg.contains("loot")) {
      return Map.of(
          "text",
          "I could not find any active deals for your mediator right now.",
          "intent",
          "search_deals");
    }

    if (msg.contains("what is")
        || msg.contains("explain")
        || msg.contains("how it works")
        || msg.contains("system")) {
      return Map.of(
          "text",
              "BUZZMA connects buyers to mediator‑published deals. You can explore deals, place orders, submit proofs, and track cashback. Ask me about **deals**, **orders**, or **tickets** anytime.",
          "intent", "unknown");
    }

    if (msg.contains("order") || msg.contains("cashback")) {
      if (hasOrders) {
        Map<String, Object> latest = request.orders().get(0);
        return Map.of(
            "text",
            "Your latest order is **"
                + str(latest.get("status"), "Pending")
                + "**. "
                + "Payment: **"
                + str(latest.get("paymentStatus"), "Pending")
                + "**, "
                + "Affiliate: **"
                + str(latest.get("affiliateStatus"), "Unchecked")
                + "**.",
            "intent",
            "check_order_status");
      }
      return Map.of(
          "text",
          "I could not find any orders yet. Want to explore deals?",
          "intent",
          "check_order_status");
    }

    if (msg.contains("ticket") || msg.contains("support")) {
      if (hasTickets) {
        Map<String, Object> latest =
            request.tickets().stream()
                .filter(t -> !"Feedback".equals(t.get("issueType")))
                .findFirst()
                .orElse(null);
        if (latest != null) {
          return Map.of(
              "text",
              "Your latest ticket (**"
                  + str(latest.get("issueType"), "Support")
                  + "**) is **"
                  + str(latest.get("status"), "Open")
                  + "**.",
              "intent",
              "check_ticket_status");
        }
      }
      return Map.of(
          "text",
          "No tickets found. You can create one from the Tickets tab.",
          "intent",
          "check_ticket_status");
    }

    if (msg.contains("profile") || msg.contains("wallet")) {
      return Map.of(
          "text",
          "Opening your **Profile & Wallet**.",
          "intent",
          "navigation",
          "navigateTo",
          "profile");
    }
    if (msg.contains("explore") || msg.contains("home")) {
      return Map.of(
          "text",
          "Taking you to **Explore Deals**.",
          "intent",
          "navigation",
          "navigateTo",
          msg.contains("home") ? "home" : "explore");
    }

    // ── Gemini fallback ──────────────────────────────────────────────────────

    if (geminiApiKey != null && !geminiApiKey.isBlank()) {
      try {
        return callGemini(request, products, rawMessage);
      } catch (Exception e) {
        LOGGER.error("Gemini fallback failed for /api/ai/chat userId={}", actorId, e);
        // fall through to default
      }
    }

    return Map.of(
        "text",
            "I’m here to help! You can ask me about **deals**, **orders**, **tickets**, or **your profile**.",
        "intent", "unknown");
  }

  private Map<String, Object> callGemini(
      ChatRequest request, List<Map<String, Object>> products, String message) throws Exception {

    String rawUserName =
        request.userName() != null
            ? request.userName().replaceAll("[\n\r\t{}\\[\\]<>]", "").strip()
            : "Guest";
    String userName = rawUserName.substring(0, Math.min(60, rawUserName.length()));
    if (userName.isBlank()) userName = "Guest";

    // Build deal context (up to 10 products)
    StringBuilder dealContext = new StringBuilder();
    for (int i = 0; i < Math.min(10, products.size()); i++) {
      Map<String, Object> p = products.get(i);
      dealContext
          .append("[ID: ")
          .append(p.get("id"))
          .append("] ")
          .append(p.get("title"))
          .append(" - Price: ₹")
          .append(toDouble(p.get("price")))
          .append(" on ")
          .append(p.get("platform"))
          .append("\n");
    }

    String ordersJson =
        objectMapper.writeValueAsString(
            request.orders() != null
                ? request.orders().subList(0, Math.min(3, request.orders().size()))
                : List.of());
    String ticketsJson =
        objectMapper.writeValueAsString(
            request.tickets() != null
                ? request.tickets().subList(0, Math.min(2, request.tickets().size()))
                : List.of());

    boolean hasImage = request.image() != null && !request.image().isBlank();

    String systemPrompt =
        buildSystemPrompt(userName, dealContext.toString(), ordersJson, ticketsJson, hasImage);

    // Build contents array
    List<Map<String, Object>> contentParts = new ArrayList<>();
    if (hasImage) {
      String imageData =
          request.image().contains(",") ? request.image().split(",", 2)[1] : request.image();
      contentParts.add(Map.of("inlineData", Map.of("mimeType", "image/jpeg", "data", imageData)));
      contentParts.add(Map.of("text", message.isEmpty() ? "Analyze this image." : message));
    } else {
      if (request.history() != null) {
        for (Map<String, Object> h : request.history()) {
          contentParts.add(Map.of("text", "[" + h.get("role") + "] " + h.get("content")));
        }
      }
      contentParts.add(Map.of("text", message.isEmpty() ? "Hello" : message));
    }

    Map<String, Object> body =
        Map.of(
            "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
            "contents", List.of(Map.of("parts", contentParts)),
            "generationConfig",
                Map.of("maxOutputTokens", 512, "responseMimeType", "application/json"));

    String bodyJson = objectMapper.writeValueAsString(body);
    Exception lastError = null;

    List<String> modelsToTry = new ArrayList<>();
    modelsToTry.add(geminiModel);
    for (String m : MODEL_FALLBACKS) {
      if (!m.equals(geminiModel)) modelsToTry.add(m);
    }

    for (String model : modelsToTry) {
      try {
        String url = GEMINI_BASE + model + ":generateContent?key=" + geminiApiKey;
        HttpRequest httpRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> response =
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
          LOGGER.warn("Gemini request failed: model={}, status={}", model, response.statusCode());
          lastError = new RuntimeException("Gemini HTTP " + response.statusCode());
          continue;
        }

        return parseGeminiResponse(response.body(), products);
      } catch (Exception e) {
        LOGGER.warn("Gemini request exception for model={}", model, e);
        lastError = e;
      }
    }
    LOGGER.error("Gemini unavailable after trying models={}", modelsToTry, lastError);
    throw lastError != null ? lastError : new RuntimeException("Gemini unavailable");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseGeminiResponse(String body, List<Map<String, Object>> products)
      throws Exception {
    Map<String, Object> root = objectMapper.readValue(body, Map.class);
    List<Map<String, Object>> candidates = (List<Map<String, Object>>) root.get("candidates");
    if (candidates == null || candidates.isEmpty()) {
      return fallbackResponse();
    }
    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
    String text = parts.isEmpty() ? "" : String.valueOf(parts.get(0).get("text"));

    Map<String, Object> parsed;
    try {
      parsed = objectMapper.readValue(text, Map.class);
    } catch (Exception e) {
      LOGGER.warn("Gemini content is not valid JSON; returning plain text response", e);
      return Map.of(
          "text",
          text.isBlank()
              ? "I’m here to help with deals, orders, or tickets. What would you like?"
              : text,
          "intent",
          "unknown");
    }

    String responseText =
        str(
            parsed.get("responseText"),
            "I’m here to help with deals, orders, or tickets. What would you like?");
    String intent = str(parsed.get("intent"), "unknown");
    String navigateTo =
        parsed.get("navigateTo") != null ? String.valueOf(parsed.get("navigateTo")) : null;

    // Resolve recommended products
    List<Map<String, Object>> recommended = List.of();
    Object ids = parsed.get("recommendedProductIds");
    if (ids instanceof List<?> idList && !idList.isEmpty()) {
      recommended =
          products.stream()
              .filter(p -> p.get("id") != null && idList.contains(String.valueOf(p.get("id"))))
              .toList();
    }
    if ("search_deals".equals(intent) && recommended.isEmpty() && !products.isEmpty()) {
      recommended = products.subList(0, Math.min(5, products.size()));
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("text", responseText);
    result.put("intent", intent);
    if (navigateTo != null) result.put("navigateTo", navigateTo);
    if (!recommended.isEmpty()) {
      result.put("uiType", "product_card");
      result.put("data", recommended);
    }

    // extractedValues for image responses
    Object ev = parsed.get("extractedValues");
    if (ev instanceof Map<?, ?> evMap && !evMap.isEmpty()) {
      Map<String, String> cleaned = new LinkedHashMap<>();
      for (String key :
          List.of(
              "orderId",
              "amount",
              "orderDate",
              "seller",
              "productName",
              "paymentMethod",
              "platform")) {
        Object val = evMap.get(key);
        if (val != null && !String.valueOf(val).isBlank()) {
          cleaned.put(key, String.valueOf(val).trim());
        }
      }
      if (!cleaned.isEmpty()) result.put("extractedValues", cleaned);
    }

    return result;
  }

  private String buildSystemPrompt(
      String userName, String deals, String orders, String tickets, boolean hasImage) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are 'BUZZMA', a world-class AI shopping strategist for ")
        .append(userName)
        .append(".\n\n");
    sb.append("CONTEXT:\n");
    sb.append("- DEALS: ").append(deals).append("\n");
    sb.append("- RECENT ORDERS: ").append(orders).append("\n");
    sb.append("- TICKETS: ").append(tickets).append("\n\n");
    sb.append("BEHAVIOR:\n");
    sb.append("1. Be concise and friendly.\n");
    sb.append(
        "2. If user mentions deals or products, put matching IDs in 'recommendedProductIds'.\n");
    sb.append(
        "3. Classify intent: 'search_deals', 'check_order_status', 'check_ticket_status', 'navigation', 'greeting', or 'unknown'.\n");
    sb.append("4. For navigation, use: 'home', 'explore', 'orders', 'profile'.\n");
    sb.append("5. Use **bold** for key info like **₹599** or **Delivered**.\n");
    sb.append(
        "6. Always respond in JSON: { responseText, intent, navigateTo?, recommendedProductIds?, extractedValues? }\n");
    if (hasImage) {
      sb.append(
          "7. IMAGE ANALYSIS (HIGHEST PRIORITY): Extract orderId, amount, orderDate, seller, productName, paymentMethod, platform from the image into extractedValues. If you find an Order ID, start responseText with 'Found Order ID: **<ID>**'.\n");
    }
    return sb.toString();
  }

  private Map<String, Object> fallbackResponse() {
    return Map.of(
        "text",
        "I’m here to help with deals, orders, or tickets. What would you like?",
        "intent",
        "unknown");
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private List<Map<String, Object>> resolveProducts(
      UUID actorId, List<Map<String, Object>> clientProducts) {
    if (clientProducts != null && !clientProducts.isEmpty()) return clientProducts;
    UsersEntity user = usersRepository.findById(actorId).orElse(null);
    if (user == null || user.getParentCode() == null || user.getParentCode().isBlank())
      return List.of();
    List<DealsEntity> deals =
        dealsRepository
            .findActiveProductsForMediator(user.getParentCode(), PageRequest.of(0, 50))
            .getContent();
    List<Map<String, Object>> result = new ArrayList<>();
    for (DealsEntity d : deals) {
      Map<String, Object> p = new LinkedHashMap<>();
      p.put("id", d.getId());
      p.put("title", d.getTitle());
      p.put("price", d.getPricePaise() != null ? d.getPricePaise() / 100.0 : 0);
      p.put(
          "originalPrice",
          d.getOriginalPricePaise() != null ? d.getOriginalPricePaise() / 100.0 : 0);
      p.put("platform", d.getPlatform());
      p.put("brandName", d.getBrandName());
      p.put("image", d.getImage());
      p.put("dealType", d.getDealType() != null ? d.getDealType().name() : "Rating");
      p.put("mediatorCode", d.getMediatorCode());
      p.put("campaignId", d.getCampaignId());
      p.put("active", d.getActive());
      result.add(p);
    }
    return result;
  }

  private Map<String, Object> findBestMatch(List<Map<String, Object>> products, String msg) {
    String[] tokens = msg.replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
    List<String> meaningful = new ArrayList<>();
    for (String t : tokens) if (t.length() > 3) meaningful.add(t);
    if (meaningful.isEmpty()) return null;
    Map<String, Object> best = null;
    int bestScore = 0;
    for (Map<String, Object> p : products) {
      String title = String.valueOf(p.getOrDefault("title", "")).toLowerCase();
      int score = 0;
      for (String t : meaningful) if (title.contains(t)) score++;
      if (title.contains(String.join(" ", meaningful))) score += 3;
      if (score > bestScore) {
        bestScore = score;
        best = p;
      }
    }
    return bestScore >= 2 ? best : null;
  }

  private int extractCount(String msg) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("top\\s+(\\d{1,2})").matcher(msg);
    if (m.find()) return Math.max(1, Math.min(50, Integer.parseInt(m.group(1))));
    m = java.util.regex.Pattern.compile("(\\d{1,2})\\s+deals").matcher(msg);
    if (m.find()) return Math.max(1, Math.min(50, Integer.parseInt(m.group(1))));
    return 0;
  }

  private double toDouble(Object val) {
    if (val instanceof Number n) return n.doubleValue();
    try {
      return Double.parseDouble(String.valueOf(val));
    } catch (Exception e) {
      return 0;
    }
  }

  private String str(Object val, String def) {
    if (val == null) return def;
    String s = String.valueOf(val).trim();
    return s.isEmpty() ? def : s;
  }

  // ── Extract Order ────────────────────────────────────────────────────────

  @PostMapping("/extract-order")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> extractOrder(
      @RequestBody ExtractOrderRequest request, @CurrentUserId UUID actorId) {

    if (request.imageBase64() == null || request.imageBase64().isBlank()) {
      return emptyExtraction("No image provided. Please enter your order details manually.");
    }

    // Hard size gate — ~4 MB base64
    if (request.imageBase64().length() > 5_000_000) {
      return emptyExtraction("Image too large. Please upload a smaller screenshot.");
    }

    if (geminiApiKey == null || geminiApiKey.isBlank()) {
      return emptyExtraction(
          "AI extraction is not configured. Please enter your order details manually.");
    }

    try {
      return callGeminiExtract(request.imageBase64());
    } catch (Exception e) {
      LOGGER.error("Gemini extract-order failed for userId={}", actorId, e);
      return emptyExtraction(
          "Extraction encountered an issue: "
              + e.getMessage().substring(0, Math.min(200, e.getMessage().length()))
              + ". Please enter details manually.");
    }
  }

  private Map<String, Object> callGeminiExtract(String imageBase64) throws Exception {
    String imageData = imageBase64.contains(",") ? imageBase64.split(",", 2)[1] : imageBase64;

    String prompt =
        """
        You are an OCR extraction assistant. Analyze this e-commerce order screenshot and extract the following fields.
        Return ONLY valid JSON with these exact keys (use null for any field you cannot find):
        {
          "orderId": "<the order ID / order number string, or null>",
          "amount": <the final amount paid as a number (not string), or null>,
          "orderDate": "<date string in YYYY-MM-DD format if possible, or as found, or null>",
          "soldBy": "<seller / sold by name, or null>",
          "productName": "<main product name, or null>",
          "accountName": "<account name / buyer name if visible, or null>",
          "platform": "<platform name: Amazon, Flipkart, Myntra, Meesho, Ajio, Nykaa, etc., or null>",
          "confidenceScore": <a number 0-100 indicating how confident you are in the extraction>
        }
        Extract the FINAL PAID amount (not MRP/original price). For orderId, prefer platform-specific formats.
        """;

    List<Map<String, Object>> parts =
        List.of(
            Map.of("inlineData", Map.of("mimeType", "image/jpeg", "data", imageData)),
            Map.of("text", prompt));

    Map<String, Object> body =
        Map.of(
            "contents", List.of(Map.of("parts", parts)),
            "generationConfig",
                Map.of("maxOutputTokens", 512, "responseMimeType", "application/json"));

    String bodyJson = objectMapper.writeValueAsString(body);
    Exception lastError = null;

    List<String> modelsToTry = new ArrayList<>();
    modelsToTry.add(geminiModel);
    for (String m : MODEL_FALLBACKS) {
      if (!m.equals(geminiModel)) modelsToTry.add(m);
    }

    for (String model : modelsToTry) {
      try {
        String url = GEMINI_BASE + model + ":generateContent?key=" + geminiApiKey;
        HttpRequest httpRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response =
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
          LOGGER.warn(
              "Gemini extract-order failed: model={}, status={}", model, response.statusCode());
          lastError = new RuntimeException("Gemini HTTP " + response.statusCode());
          continue;
        }

        return parseExtractResponse(response.body());
      } catch (Exception e) {
        LOGGER.warn("Gemini extract-order exception for model={}", model, e);
        lastError = e;
      }
    }
    throw lastError != null ? lastError : new RuntimeException("Gemini unavailable");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseExtractResponse(String body) throws Exception {
    Map<String, Object> root = objectMapper.readValue(body, Map.class);
    List<Map<String, Object>> candidates = (List<Map<String, Object>>) root.get("candidates");
    if (candidates == null || candidates.isEmpty()) {
      return emptyExtraction(
          "Could not extract order details from the image. Please enter details manually.");
    }
    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
    String text = parts.isEmpty() ? "" : String.valueOf(parts.get(0).get("text"));

    Map<String, Object> parsed;
    try {
      parsed = objectMapper.readValue(text, Map.class);
    } catch (Exception e) {
      LOGGER.warn("Gemini extract-order response is not valid JSON", e);
      return emptyExtraction("Could not parse extraction result. Please enter details manually.");
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("orderId", parsed.getOrDefault("orderId", null));
    result.put("amount", parsed.getOrDefault("amount", null));
    result.put("orderDate", parsed.getOrDefault("orderDate", null));
    result.put("soldBy", parsed.getOrDefault("soldBy", null));
    result.put("productName", parsed.getOrDefault("productName", null));
    result.put("accountName", parsed.getOrDefault("accountName", null));
    result.put("platform", parsed.getOrDefault("platform", null));

    Object cs = parsed.get("confidenceScore");
    int confidence = 0;
    if (cs instanceof Number n) confidence = n.intValue();
    result.put("confidenceScore", confidence);

    if (result.values().stream().filter(v -> v != null && !v.equals(0)).count() <= 1) {
      result.put(
          "notes", "Limited information extracted. Please verify and fill in missing details.");
    }
    return result;
  }

  private Map<String, Object> emptyExtraction(String notes) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("orderId", null);
    result.put("amount", null);
    result.put("orderDate", null);
    result.put("soldBy", null);
    result.put("productName", null);
    result.put("accountName", null);
    result.put("platform", null);
    result.put("confidenceScore", 0);
    result.put("notes", notes);
    return result;
  }

  public record ChatRequest(
      String message,
      String userName,
      @Size(max = 200) List<Map<String, Object>> products,
      @Size(max = 200) List<Map<String, Object>> orders,
      @Size(max = 200) List<Map<String, Object>> tickets,
      List<Map<String, Object>> history,
      String image) {}

  public record ExtractOrderRequest(String imageBase64) {}
}
