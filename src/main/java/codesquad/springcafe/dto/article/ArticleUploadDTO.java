package codesquad.springcafe.dto.article;

import codesquad.springcafe.model.Article;
import java.time.LocalDateTime;

public class ArticleUploadDTO {
    private final String writer;
    private final String title;
    private final String content;

    public ArticleUploadDTO(String writer, String title, String content) {
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    public Article toArticle(Long id) {
        return new Article(id, LocalDateTime.now(), writer, title, content);
    }
}