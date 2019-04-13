package com.jitterted;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

import static com.jitterted.ChatToolWindow.TWITCH_API_CLIENT_ID_PROPERTY_KEY;
import static com.jitterted.ChatToolWindow.TWITCH_API_CLIENT_SECRET_PROPERTY_KEY;
import static com.jitterted.ChatToolWindow.TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY;
import static org.assertj.core.api.Assertions.assertThat;

public class ChatToolWindowTest {

  @Test
  public void shouldLoadProperties() throws Exception {
    Properties twitchProperties = new Properties();
    twitchProperties.load(new FileReader("/Users/ted/.twitch.properties"));
    String oAuthToken = twitchProperties.getProperty(TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY);
    assertThat(oAuthToken)
        .isNotEmpty();

    String twitchClientId = twitchProperties.getProperty(TWITCH_API_CLIENT_ID_PROPERTY_KEY);
    assertThat(twitchClientId)
        .isNotEmpty();

    String twitchClientSecret = twitchProperties.getProperty(TWITCH_API_CLIENT_SECRET_PROPERTY_KEY);
    assertThat(twitchClientSecret)
        .isNotEmpty();

  }

  @Test
  public void messageShouldBeSentToChat() throws Exception {
    TwitchChat chat = null;
    try {
      Properties twitchProperties = new Properties();
      twitchProperties.load(new FileReader("/Users/ted/.twitch.properties"));

      String oAuthToken = twitchProperties.getProperty(TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY);
      String twitchClientId = twitchProperties.getProperty(TWITCH_API_CLIENT_ID_PROPERTY_KEY);
      String twitchClientSecret = twitchProperties.getProperty(TWITCH_API_CLIENT_SECRET_PROPERTY_KEY);

      OAuth2Credential credential = new OAuth2Credential("twitch", oAuthToken);

      TwitchClient twitchClient = TwitchClientBuilder.builder()
                                                     .withClientId(twitchClientId)
                                                     .withClientSecret(twitchClientSecret)
                                                     .withEnableChat(true)
                                                     .withChatAccount(credential)
                                                     .build();
      chat = twitchClient.getChat();

      chat.getEventManager().onEvent(ChannelMessageEvent.class).subscribe(event -> {
        System.out.println(event.getUser() + ":" + event.getMessage());
      });

      Thread.sleep(1000);

      chat.joinChannel("jitterted");
      chat.sendMessage("jitterted", "Testing from JitterChat");

      System.out.println("\nMessage sent\n");

      chat.sendMessage("jitterted", "Second message from test");

      Thread.sleep(60_000);

      System.out.println("Exiting test.");
    } finally {
      if (chat != null) {
        chat.disconnect();
        System.out.println("Disconnecting.");
      }
    }

  }

  @Test
  public void splitMax() throws Exception {
    String text = "comment 26 what the heck is this?";
    String[] split = StringUtils.split(text, " ", 3);
    System.out.println(Arrays.toString(split));

    text = "line 33";
    split = StringUtils.split(text, " ", 3);
    System.out.println(Arrays.toString(split));

  }

}