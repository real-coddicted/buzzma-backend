package com.coddicted.buzzma.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable {

  private final int limit;
  private final long offset;
  private final Sort sort;

  public OffsetBasedPageRequest(final int limit, final long offset, final Sort sort) {
    this.limit = limit;
    this.offset = offset;
    this.sort = sort;
  }

  @Override
  public int getPageNumber() {
    return (int) (offset / limit);
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    return new OffsetBasedPageRequest(limit, offset + limit, sort);
  }

  @Override
  public Pageable previousOrFirst() {
    return new OffsetBasedPageRequest(limit, Math.max(0, offset - limit), sort);
  }

  @Override
  public Pageable first() {
    return new OffsetBasedPageRequest(limit, 0, sort);
  }

  @Override
  public Pageable withPage(final int pageNumber) {
    return new OffsetBasedPageRequest(limit, (long) pageNumber * limit, sort);
  }

  @Override
  public boolean hasPrevious() {
    return offset > 0;
  }
}
