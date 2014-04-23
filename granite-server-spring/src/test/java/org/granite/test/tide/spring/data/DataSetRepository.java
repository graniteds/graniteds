package org.granite.test.tide.spring.data;

import org.granite.tide.spring.data.FilterableJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSetRepository extends FilterableJpaRepository<DataSet, Long> {

}
