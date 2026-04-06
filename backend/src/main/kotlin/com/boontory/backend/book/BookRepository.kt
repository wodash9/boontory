package com.boontory.backend.book

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<BookEntity, Long> {
    fun findByIsbn(isbn: String): BookEntity?

    fun countByShelfId(shelfId: Long): Long

    @Query(
        """
        select b from BookEntity b
        where (:status is null or b.status = :status)
          and (:shelfId is null or b.shelf.id = :shelfId)
          and (
            :query is null
            or lower(b.title) like lower(concat('%', :query, '%'))
            or lower(b.authors) like lower(concat('%', :query, '%'))
          )
        order by b.createdAt desc
        """,
    )
    fun search(
        @Param("query") query: String?,
        @Param("status") status: ReadingStatus?,
        @Param("shelfId") shelfId: Long?,
    ): List<BookEntity>

    @Modifying
    @Query("update BookEntity b set b.shelf = null where b.shelf.id = :shelfId")
    fun clearShelfForShelf(@Param("shelfId") shelfId: Long)
}
