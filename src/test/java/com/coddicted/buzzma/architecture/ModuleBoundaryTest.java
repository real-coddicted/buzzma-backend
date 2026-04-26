package com.coddicted.buzzma.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

class ModuleBoundaryTest {

  private static final String ROOT = "com.coddicted.buzzma";

  private static final Set<String> MODULES =
      Set.of(
          "brands",
          "agency",
          "mediator",
          "buyers",
          "catalog",
          "orders",
          "wallet",
          "support",
          "identity",
          "admin",
          "notifications",
          "shared",
          "ai",
          "ops");

  /** Classes allowed to live directly at com.coddicted.buzzma root (outside any module). */
  private static final Set<String> ROOT_WHITELIST =
      Set.of("com.coddicted.buzzma.BuzzmaBackendApplication");

  /**
   * Fully-qualified classes allowed to import from other modules' internal (non-api, non-shared)
   * packages. These are framework wiring points or Spring Security principal couplings that cannot
   * be expressed as DTO-only ports.
   */
  private static final Set<String> CROSS_MODULE_IMPORT_WHITELIST =
      Set.of(
          "com.coddicted.buzzma.shared.security.SecurityConfig",
          "com.coddicted.buzzma.mediator.security.UpstreamSuspensionFilter");

  private static JavaClasses productionClasses;

  @BeforeAll
  static void importClasses() {
    productionClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages(ROOT);
  }

  @Test
  void everyClassResidesInOneOfTheTwelveModulesOrIsWhitelistedAtRoot() {
    classes()
        .that()
        .resideInAPackage("com.coddicted.buzzma..")
        .should(
            new com.tngtech.archunit.lang.ArchCondition<JavaClass>(
                "reside in com.coddicted.buzzma.<module>.* where <module> is one of the known modules, or be whitelisted at com.coddicted.buzzma root") {
              @Override
              public void check(JavaClass item, com.tngtech.archunit.lang.ConditionEvents events) {
                String fqn = item.getName();
                if (ROOT_WHITELIST.contains(fqn)) {
                  return;
                }
                String pkg = item.getPackageName();
                if (!pkg.startsWith(ROOT + ".")) {
                  events.add(
                      com.tngtech.archunit.lang.SimpleConditionEvent.violated(
                          item, fqn + " is not under com.coddicted.buzzma"));
                  return;
                }
                String afterRoot = pkg.substring((ROOT + ".").length());
                String firstSegment =
                    afterRoot.contains(".")
                        ? afterRoot.substring(0, afterRoot.indexOf('.'))
                        : afterRoot;
                if (!MODULES.contains(firstSegment)) {
                  events.add(
                      com.tngtech.archunit.lang.SimpleConditionEvent.violated(
                          item,
                          fqn + " is in package '" + pkg + "' which is not one of the 12 modules"));
                }
              }
            })
        .check(productionClasses);
  }

  @Test
  void entitiesResideInPersistencePackages() {
    classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .should()
        .resideInAPackage("com.coddicted.buzzma..persistence..")
        .check(productionClasses);
  }

  @Test
  void repositoriesResideInPersistencePackages() {
    classes()
        .that()
        .areAssignableTo(Repository.class)
        .and()
        .resideInAPackage("com.coddicted.buzzma..")
        .should()
        .resideInAPackage("com.coddicted.buzzma..persistence..")
        .check(productionClasses);
  }

  @Test
  void restControllersResideInWebPackages() {
    classes()
        .that()
        .areAnnotatedWith(RestController.class)
        .should()
        .resideInAPackage("com.coddicted.buzzma..web..")
        .check(productionClasses);
  }

  @Test
  void mapstructMappersResideInMapperPackages() {
    classes()
        .that()
        .areAnnotatedWith(Mapper.class)
        .should()
        .resideInAPackage("com.coddicted.buzzma..mapper..")
        .check(productionClasses);
  }

  @Test
  void sharedModuleContainsNoEntitiesOrRepositories() {
    noClasses()
        .that()
        .resideInAPackage("com.coddicted.buzzma.shared..")
        .should()
        .beAnnotatedWith(Entity.class)
        .orShould()
        .beAssignableTo(Repository.class)
        .check(productionClasses);
  }

  @Test
  void crossModuleImportsOnlyReachApiOrShared() {
    noClasses()
        .that()
        .resideInAPackage("com.coddicted.buzzma..")
        .and(
            new DescribedPredicate<JavaClass>("are not cross-module wiring whitelist") {
              @Override
              public boolean test(JavaClass clazz) {
                return !CROSS_MODULE_IMPORT_WHITELIST.contains(clazz.getName());
              }
            })
        .should(
            new com.tngtech.archunit.lang.ArchCondition<JavaClass>(
                "only import from same module, com.coddicted.buzzma.<other>.api.*, or com.coddicted.buzzma.shared.*") {
              @Override
              public void check(JavaClass item, com.tngtech.archunit.lang.ConditionEvents events) {
                String ownModule = moduleOf(item);
                if (ownModule == null) {
                  return; // Root-whitelisted classes handled by separate rule
                }
                item.getDirectDependenciesFromSelf()
                    .forEach(
                        dep -> {
                          JavaClass target = dep.getTargetClass();
                          String targetName = target.getName();
                          if (!targetName.startsWith(ROOT + ".")) {
                            return; // JDK / framework / third-party
                          }
                          String targetModule = moduleOf(target);
                          if (targetModule == null) {
                            return; // root-whitelisted target
                          }
                          if (targetModule.equals(ownModule)) {
                            return; // same module
                          }
                          if (targetModule.equals("shared")) {
                            return; // shared is universally allowed
                          }
                          String targetAfterModule =
                              target
                                  .getPackageName()
                                  .substring((ROOT + "." + targetModule + ".").length());
                          String firstSubPkg =
                              targetAfterModule.contains(".")
                                  ? targetAfterModule.substring(0, targetAfterModule.indexOf('.'))
                                  : targetAfterModule;
                          if ("api".equals(firstSubPkg)) {
                            return; // cross-module api access is allowed
                          }
                          events.add(
                              com.tngtech.archunit.lang.SimpleConditionEvent.violated(
                                  item,
                                  item.getName()
                                      + " (module="
                                      + ownModule
                                      + ") reaches into "
                                      + targetName
                                      + " (module="
                                      + targetModule
                                      + ", sub="
                                      + firstSubPkg
                                      + ")"));
                        });
              }
            })
        .check(productionClasses);
  }

  private static String moduleOf(JavaClass clazz) {
    if (ROOT_WHITELIST.contains(clazz.getName())) {
      return null;
    }
    String pkg = clazz.getPackageName();
    if (!pkg.startsWith(ROOT + ".")) {
      return null;
    }
    String afterRoot = pkg.substring((ROOT + ".").length());
    String firstSegment =
        afterRoot.contains(".") ? afterRoot.substring(0, afterRoot.indexOf('.')) : afterRoot;
    return MODULES.contains(firstSegment) ? firstSegment : null;
  }
}
