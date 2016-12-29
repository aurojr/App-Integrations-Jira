/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.webhook.jira.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;

import com.symphony.api.pod.client.ApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class to validate {@link IssueCreatedJiraParser}
 *
 * Created by rsanchez on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueCreatedJiraParsetTest extends JiraParserTest {

  private static final String FILENAME = "jiraCallbackSampleIssueCreated.json";
  public static final String FILE_ISSUE_CREATED_WITH_EPIC =
      "jiraCallbackSampleIssueCreatedWithEpic.json";

  @InjectMocks
  private IssueCreatedJiraParser issueCreated = new IssueCreatedJiraParser();

  @Test
  public void testIssueCreated() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    User returnedUser = new User();
    returnedUser.setEmailAddress("test2@symphony.com");
    returnedUser.setId(123l);
    returnedUser.setUserName("test2");
    returnedUser.setDisplayName("Test2 User");
    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME));
    String result = issueCreated.parse(parameters, node);

    assertNotNull(result);
    String expected = readFile("parser/issueCreatedJiraParser/issueCreatedMessageML.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIssueCreatedUnassigned() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME));
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);
    fieldsNode.remove(ASSIGNEE_PATH);
    fieldsNode.putNull(ASSIGNEE_PATH);

    String result = issueCreated.parse(parameters, node);

    assertNotNull(result);

    String expected = readFile("parser/issueCreatedJiraParser/issueCreatedUnassigneeMessageML.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testIssueCreatedWithEpic() throws IOException, JiraParserException, ApiException {
    User user = new User();
    user.setEmailAddress("test@symphony.com");
    doReturn(user).when(userService).getUserByEmail(anyString(), eq("test@symphony.com"));

    User user2 = new User();
    user2.setEmailAddress("test2@symphony.com");
    doReturn(user2).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));

    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    ObjectNode node = (ObjectNode) JsonUtils.readTree(classLoader.getResourceAsStream(
        FILE_ISSUE_CREATED_WITH_EPIC));

    String result = issueCreated.parse(parameters, node);
    String expected = readFile("parser/issueCreatedJiraParser/issueCreatedWithEpicMessageML.xml");
    assertEquals(expected, result);
  }
}