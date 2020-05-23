package com.mightyjava.repository;

import com.mightyjava.domain.SearchQuery;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookRepository extends PagingAndSortingRepository<SearchQuery, Long> {

    @Query("From SearchQuery b where b.Rank > 0.5 and b.Rank=:id")
    Page<SearchQuery> findAllBooks(Pageable pageable,@Param("id") int id);
}