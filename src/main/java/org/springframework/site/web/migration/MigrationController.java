package org.springframework.site.web.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.site.domain.blog.BlogService;
import org.springframework.site.domain.blog.Post;
import org.springframework.site.domain.blog.PostForm;
import org.springframework.site.domain.team.MemberProfile;
import org.springframework.site.domain.team.TeamService;
import org.springframework.site.web.SiteUrl;
import org.springframework.site.web.blog.PostView;
import org.springframework.site.web.blog.PostViewFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class MigrationController {

	private SiteUrl siteUrl;
	private final PostViewFactory postViewFactory;

	private final TeamService teamService;
	private final BlogService blogService;

	@Autowired
	public MigrationController(TeamService teamService, BlogService blogService, SiteUrl siteUrl, PostViewFactory postViewFactory) {
		this.teamService = teamService;
		this.blogService = blogService;
		this.siteUrl = siteUrl;
		this.postViewFactory = postViewFactory;
	}

	@RequestMapping(value = "/migration/profile", method = POST)
	public void migrateTeamMember(HttpServletResponse response, MemberProfile profile) {
		MemberProfile existingProfile = teamService.fetchMemberProfile(profile.getMemberId());
		response.setContentLength(0);
		response.setStatus(200);
		response.setHeader("Location", siteUrl.getAbsoluteUrl("/about/team/" + profile.getMemberId()));
		if (existingProfile == null) {
			profile.setHidden(true);
			teamService.saveMemberProfile(profile);
			response.setStatus(201);
		}
	}

	@RequestMapping(value = "/migration/blogpost", method = POST)
	public void migrateBlogPost(HttpServletResponse response, PostForm postForm) {
		Post post = blogService.getPost(postForm.getTitle(), postForm.getCreatedAt());
		response.setContentLength(0);
		if (post == null) {
			post = blogService.addPost(postForm, postForm.getAuthorMemberId());
			response.setStatus(201);
		} else {
			blogService.updatePost(post, postForm);
			response.setStatus(200);
		}
		PostView postView = postViewFactory.createPostView(post);
		response.setHeader("Location", siteUrl.getAbsoluteUrl(postView.getPath()));
	}

}
