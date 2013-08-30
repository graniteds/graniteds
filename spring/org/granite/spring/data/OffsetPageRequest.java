package org.granite.spring.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {
    
    private int offset;
    private int pageSize;
    private Sort sort;
    
    public OffsetPageRequest(int offset, int pageSize, Sort sort) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public int getOffset() {
        return offset;
    }

    public int getPageNumber() {
        return pageSize > 0 ? offset / pageSize : 0;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Sort getSort() {
        return sort;
    }
}