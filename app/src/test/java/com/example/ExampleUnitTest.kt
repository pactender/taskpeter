package com.example

import com.example.data.auth.GitHubAuthState
import com.example.data.github.GitHubService
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TaskPeter data models and utilities.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun gitHubAuthState_defaults() {
    val unauth = GitHubAuthState.Unauthenticated
    assertEquals(GitHubAuthState.Unauthenticated, unauth)

    val authenticated = GitHubAuthState.Authenticated(
      uid = "gh_test_123",
      displayName = "Linus Dev",
      email = "linus@taskpeter.dev",
      photoUrl = "https://github.com/torvalds.png",
      githubUsername = "torvalds"
    )
    assertEquals("torvalds", authenticated.githubUsername)
    assertEquals("github.com", authenticated.providerId)
  }

  @Test
  fun gitHubService_commandGeneration() {
    val cmd = GitHubService.generateGitCommitCommand("feat: add firebase auth", "feat/auth")
    assertTrue(cmd.contains("git checkout -b feat/auth"))
    assertTrue(cmd.contains("git commit -m \"feat: add firebase auth\""))
  }
}
