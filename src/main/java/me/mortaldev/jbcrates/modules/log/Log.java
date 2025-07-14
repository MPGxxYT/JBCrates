package me.mortaldev.jbcrates.modules.log;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
  // [14:40:56] [FAILED] kylebomb gave %reciever% crate %crate_id%
  public static final String FILE_LOG_FORMAT = "[{0}] [{1}] {2} gave {3} crate {4}";
  // [FAILED] kylebomb gave %reciever% crate %crate_id%
  public static final String CONSOLE_LOG_FORMAT = "[{0}] {1} gave {2} crate {3}";
  public static final String CHAT_LOG_FORMAT = "&f[{0}&f] &e{1}&f gave &e{2}&f crate &e{3}";
  public static final String LOG_TIME_FORMAT = "h:mm:ss a";
  private final LocalDateTime dateTime;
  private final Status status;
  private final String sender;
  private final String receiver;
  private final String crateID;

  public Log(Status status, String sender, String receiver, String crateID) {
    this.dateTime = LocalDateTime.now();
    this.status = status;
    this.sender = sender;
    this.receiver = receiver;
    this.crateID = crateID;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public String formFileLog() {
    String time = dateTime.format(DateTimeFormatter.ofPattern(LOG_TIME_FORMAT));
    return MessageFormat.format(FILE_LOG_FORMAT, time, status, sender, receiver, crateID);
  }

  public String formConsoleLog() {
    return MessageFormat.format(CONSOLE_LOG_FORMAT, status, sender, receiver, crateID);
  }

  public String formChatLog() {
    return MessageFormat.format(CHAT_LOG_FORMAT, status.getDisplay(), sender, receiver, crateID);
  }

  public enum Status {
    RECEIVED("&aRECEIVED"),
    OFFLINE("&7OFFLINE"),
    FAILED("&cFAILED");

    final String display;

    Status(String chatDisplay) {
      this.display = chatDisplay;
    }

    public String getDisplay() {
      return display;
    }
  }
}
