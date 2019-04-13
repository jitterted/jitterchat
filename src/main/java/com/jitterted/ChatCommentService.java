package com.jitterted;

import java.util.HashMap;
import java.util.Map;

public class ChatCommentService {

  private final Map<Integer, String> lineComments = new HashMap<>();

  public boolean hasComment(int lineNumber) {
    return lineComments.containsKey(lineNumber);
  }

  public String commentForLine(int lineNumber) {
    return lineComments.get(lineNumber);
  }

  public void lineComment(int lineNumber, String comment) {
    lineComments.put(lineNumber, comment);
  }
}
