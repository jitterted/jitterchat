package com.jitterted;

import com.github.twitch4j.chat.TwitchChat;
import groovy.util.logging.Slf4j;

@Slf4j
public class Twitch4jTest {

    private static TwitchChat twitchChat;

//  @BeforeClass
//  public static void connectToChat() throws Exception {
//
//    Properties twitchProperties = new Properties();
//    twitchProperties.load(new FileReader("~/.twitch.properties"));
//
//    OAuth2Credential credential = new OAuth2Credential("twitch", oAuthToken);
//
//
//    // event manager
//    EventManager eventManager = new EventManager();
//
//    // credential manager
//    CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
//    credentialManager.registerIdentityProvider(new TwitchIdentityProvider(twitchClientId, twitchClientSecret, ""));
//
//    // construct twitchChat
//    twitchChat = TwitchChatBuilder.builder()
//                                  .withEventManager(eventManager)
//                                  .withCredentialManager(credentialManager)
//                                  .withChatAccount(credential)
//                                  .withCommandTrigger("!")
//                                  .build();
//
//    // sleep for a few seconds so that we're connected
//    Thread.sleep(2000);
//    System.out.println("\n\n Connected \n\n");
//  }

//  @Test
//  public void sendTwitchChannelMessage() throws Exception {
//    // listen for events in channel
//    twitchChat.joinChannel("jitterted");
//    twitchChat.getEventManager().onEvent(ChannelMessageEvent.class).subscribe(event -> {
//      System.out.println("\n\n" + event.toString() + "\n");
//    });
//
//    // send message to channel
//    twitchChat.sendMessage("jitterted", "Hello from Twitch4j");
//
//    Thread.sleep(10_000);
//
//    // check if the message was send and received
////    assertTrue(twitchChat.ircCommandQueue.size() == 0, "Can't find the message we send in the received messages!");
//
//  }

//  @Test
//  public void localTestRun() throws Exception {
//    // listen for events in channel
//    twitchChat.joinChannel("jitterted");
//    twitchChat.getEventManager().onEvent(ChannelMessageEvent.class).subscribe(event -> {
//      System.out.println("\n\n" + event.toString() + "\n");
//    });
//
//    twitchChat.getEventManager().onEvent(CommandEvent.class).subscribe(event -> {
//      System.out.println("\n\n" + event.toString() + "\n");
//    });
//
//    twitchChat.sendMessage("jitterted", "Hello from Twitch4j");
//
//    // sleep a second and look of the message was sended
//    Thread.sleep(120_000);
//  }

}
