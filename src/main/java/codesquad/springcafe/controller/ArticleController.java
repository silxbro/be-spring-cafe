package codesquad.springcafe.controller;

import codesquad.springcafe.dto.article.ArticleInfoDTO;
import codesquad.springcafe.dto.article.ArticleUploadDTO;
import codesquad.springcafe.dto.article.ArticleUpdateDTO;
import codesquad.springcafe.dto.reply.ReplyInfoDTO;
import codesquad.springcafe.model.Article;
import codesquad.springcafe.model.Reply;
import codesquad.springcafe.service.ArticleService;
import codesquad.springcafe.service.ReplyService;
import codesquad.springcafe.util.Page;
import codesquad.springcafe.util.PageRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final ReplyService replyService;

    @Autowired
    public ArticleController(ArticleService articleService, ReplyService replyService) {
        this.articleService = articleService;
        this.replyService = replyService;
    }

    @PostMapping("articles")
    public String upload(@ModelAttribute("article") ArticleUploadDTO articleUploadDTO, HttpSession session) {
        Long lastId = articleService.getLastId();
        Article newArticle = articleUploadDTO.toArticle(++lastId);
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!newArticle.isWrittenBy(loggedInUser)) {
            return "/article/update_failed";
        }
        articleService.upload(newArticle);
        return "redirect:/";
    }

    @GetMapping(value = {"/", "/page/{pageNumber}"})
    public String showPage(Model model, @PathVariable(required = false) Long pageNumber) {
        PageRequest pageRequest = getPageRequest(pageNumber);

        List<ArticleInfoDTO> articles = articleService.findAllByPaging(pageRequest).stream()
            .map(Article::toDTO).toList();
        Page targetPage = new Page(pageRequest, articleService.getTotalCount());

        model.addAttribute("articles", articles);
        model.addAttribute("pageInfo", targetPage);

        return "index";
    }

    @GetMapping("articles/{id}")
    public String showArticle(@PathVariable("id") Long id, Model model) {
        ArticleInfoDTO targetArticle = articleService.findById(id).toDTO();
        List<ReplyInfoDTO> repliesOfArticle = replyService.findAllByArticleId(id)
            .stream().map(Reply::toDTO).toList();
        model.addAttribute("article", targetArticle);
        model.addAttribute("replies", repliesOfArticle);
        model.addAttribute("numberOfReplies", repliesOfArticle.size());
        return "/article/show";
    }

    @GetMapping("articles/{id}/update")
    public String tryUpdate(@PathVariable Long id, HttpSession session) {
        Article targetArticle = articleService.findById(id);
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!targetArticle.isWrittenBy(loggedInUser)) {
            return "/article/update_failed";
        }
        return "redirect:/articles/" + id + "/form";
    }

    @GetMapping("articles/{id}/form")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        ArticleInfoDTO originalArticle = articleService.findById(id).toDTO();
        model.addAttribute("article", originalArticle);
        return "/article/updateForm";
    }

    @PutMapping("articles/{id}/update")
    public String update(@ModelAttribute("article") ArticleUpdateDTO updateDTO, @PathVariable Long id, HttpSession session) {
        Article originalArticle = articleService.findById(id);
        Article updatedArticle = updateDTO.toArticle(originalArticle);
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (!updatedArticle.isWrittenBy(loggedInUser))  {
            return "/article/update_failed";
        }
        articleService.update(updatedArticle);
        return "redirect:/articles/{id}";
    }

    @DeleteMapping("articles/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Article targetArticle = articleService.findById(id);
        List<Reply> replies = replyService.findAllByArticleId(id);
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (!targetArticle.isWrittenBy(loggedInUser)) {
            return "/article/delete_failed_user";
        }
        if (hasExternalReplies(replies, loggedInUser)) {
            return "/article/delete_failed_reply";
        }
        articleService.delete(id);
        return "redirect:/articles/{id}";
    }

    private boolean hasExternalReplies(List<Reply> replies, String loggedInUser) {
        return !replies.isEmpty() && !replies.stream().allMatch(reply -> reply.isWrittenBy(loggedInUser));
    }

    private PageRequest getPageRequest(Long pageNumber) {
        int pageSize = 15;
        int buttonCount = 5;
        if (pageNumber == null) {
            return new PageRequest(1L, pageSize, buttonCount);
        }
        return new PageRequest(pageNumber, pageSize, buttonCount);
    }
}
