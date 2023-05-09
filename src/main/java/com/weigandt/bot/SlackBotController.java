package com.weigandt.bot;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackAppServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet("/slack/events")
public class SlackBotController extends SlackAppServlet {
    public SlackBotController(App app) {
        super(app);
    }
}
