package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

public class ConsensusServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    String validAfterParameter = request.getParameter("valid-after");

    /* Check if we have a descriptors directory. */
    // TODO make this configurable!
    File archiveDirectory = new File("/srv/metrics.torproject.org/archives");
    if (!archiveDirectory.exists() || !archiveDirectory.isDirectory()) {
      /* Oops, we don't have any descriptors to serve. */
// TODO change to internal server error
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    /* Check valid-after parameter. */
    if (validAfterParameter == null ||
        validAfterParameter.length() < "yyyy-MM-dd-HH-mm-ss".length()) {
// TODO is there something like "wrong parameter"?
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    SimpleDateFormat timeFormat = new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss");
    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date parsedTimestamp = null;
    try {
      parsedTimestamp = timeFormat.parse(validAfterParameter);
    } catch (ParseException e) {
// TODO is there something like "wrong parameter"?
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    if (parsedTimestamp == null) {
// TODO is there something like "wrong parameter"?
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String consensusFilename = archiveDirectory.getAbsolutePath()
        + "/consensuses-" + validAfterParameter.substring(0, 4) + "-"
        + validAfterParameter.substring(5, 7) + "/"
        + validAfterParameter.substring(8, 10) + "/"
        + validAfterParameter + "-consensus";
    File consensusFile = new File(consensusFilename);

    if (!consensusFile.exists()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    /* Read file from disk and write it to response. */
    BufferedInputStream input = null;
    BufferedOutputStream output = null;
    try {
      response.setContentType("text/plain");
      response.setHeader("Content-Length", String.valueOf(
          consensusFile.length()));
      response.setHeader("Content-Disposition",
          "inline; filename=\"" + consensusFile.getName() + "\"");
      input = new BufferedInputStream(new FileInputStream(consensusFile),
          1024);
      output = new BufferedOutputStream(response.getOutputStream(), 1024);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = input.read(buffer)) > 0) {
          output.write(buffer, 0, length);
      }
    } finally {
      output.close();
      input.close();
    }
  }
}

