package com.jitterted;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.CommandEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import groovy.util.logging.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.util.Properties;

import static com.jitterted.ChatToolWindow.TWITCH_API_CLIENT_ID_PROPERTY_KEY;
import static com.jitterted.ChatToolWindow.TWITCH_API_CLIENT_SECRET_PROPERTY_KEY;
import static com.jitterted.ChatToolWindow.TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY;

@Slf4j
public class Twitch4jTest {

  private static TwitchChat twitchChat;

  @BeforeClass
  public static void connectToChat() throws Exception {

    Properties twitchProperties = new Properties();
    twitchProperties.load(new FileReader("/Users/ted/.twitch.properties"));

    String oAuthToken = twitchProperties.getProperty(TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY);
    String twitchClientId = twitchProperties.getProperty(TWITCH_API_CLIENT_ID_PROPERTY_KEY);
    String twitchClientSecret = twitchProperties.getProperty(TWITCH_API_CLIENT_SECRET_PROPERTY_KEY);

    OAuth2Credential credential = new OAuth2Credential("twitch", oAuthToken);


    // event manager
    EventManager eventManager = new EventManager();

    // credential manager
    CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
    credentialManager.registerIdentityProvider(new TwitchIdentityProvider(twitchClientId, twitchClientSecret, ""));

    // construct twitchChat
    twitchChat = TwitchChatBuilder.builder()
                                  .withEventManager(eventManager)
                                  .withCredentialManager(credentialManager)
                                  .withChatAccount(credential)
                                  .withCommandTrigger("!")
                                  .build();

    // sleep for a few seconds so that we're connected
    Thread.sleep(2000);
    System.out.println("\n\n Connected \n\n");
  }

  @Test
  public void sendTwitchChannelMessage() throws Exception {
    // listen for events in channel
    twitchChat.joinChannel("jitterted");
    twitchChat.getEventManager().onEvent(ChannelMessageEvent.class).subscribe(event -> {
      System.out.println("\n\n" + event.toString() + "\n");
    });

    // send message to channel
    twitchChat.sendMessage("jitterted", "Hello from Twitch4j");

    Thread.sleep(10_000);

    // check if the message was send and received
//    assertTrue(twitchChat.ircCommandQueue.size() == 0, "Can't find the message we send in the received messages!");

  }

  @Test
  public void localTestRun() throws Exception {
    // listen for events in channel
    twitchChat.joinChannel("jitterted");
    twitchChat.getEventManager().onEvent(ChannelMessageEvent.class).subscribe(event -> {
      System.out.println("\n\n" + event.toString() + "\n");
    });

    twitchChat.getEventManager().onEvent(CommandEvent.class).subscribe(event -> {
      System.out.println("\n\n" + event.toString() + "\n");
    });

    twitchChat.sendMessage("jitterted", "Hello from Twitch4j");

    // sleep a second and look of the message was sended
    Thread.sleep(120_000);
  }

}
