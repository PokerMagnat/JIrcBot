package ru.bvn13.jircbot.database.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * Created by bvn13 on 03.02.2018.
 */
@Entity
@Table(name = "grammar_correction", uniqueConstraints = {@UniqueConstraint(columnNames = {"word"}, name = "uniq_grammar_correction_word")})
@NoArgsConstructor
@AllArgsConstructor
public class GrammarCorrection extends BaseModel {

    @Getter
    @Setter
    @Column(nullable = false)
    private String word;

    @Getter
    @Setter
    @Column(nullable = false)
    private String correction;

    @Getter
    @Setter
    private String author;

    public GrammarCorrection(Long id, Date dtCreated, Date dtUpdated, String word, String correction, String author) {
        this.setId(id);
        this.setDtCreated(dtCreated);
        this.setDtUpdated(dtUpdated);
        this.setWord(word);
        this.setCorrection(correction);
        this.setAuthor(author);
    }



}
