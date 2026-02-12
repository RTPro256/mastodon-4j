package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "filter_keywords")
public class FilterKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "filter_id", nullable = false)
    private Filter filter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String keyword;

    @Column(name = "whole_word", nullable = false)
    private boolean wholeWord = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }
}
