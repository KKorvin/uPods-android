package com.chickenkiller.upods2.controllers.internet;

import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.utils.GlobalUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class EpisodesXMLHandler extends DefaultHandler {
    private final String ITEM_TITLE = "item";
    private final String TITLE = "title";
    private final String LENGTH = "length";
    private final String SUMMARY1 = "content:encoded";
    private final String SUMMARY2 = "description";
    private final String SUMMARY3 = "itunes:summary";
    private final String MP3_1 = "enclosure";
    private final String MP3_2 = "link";
    private final String MP3_3 = "url";
    private final String PUBDATE = "pubDate";
    private final String DURATION1 = "itunes:duration";
    private final String DURATION2 = "duration";

    private boolean elementOn = false;
    private boolean isItem = false;

    private StringBuilder elementValue = new StringBuilder("");
    private Episode episode = null;
    private ArrayList<Episode> allEpisodes;

    private String podcastSummary;

    public EpisodesXMLHandler() {
        this.podcastSummary = "";
        allEpisodes = new ArrayList<>();
    }

    /**
     * This will be called when the tags of the XML starts.
     **/
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        elementValue = new StringBuilder("");
        elementOn = true;
        if (localName.equals(ITEM_TITLE)) {
            episode = new Episode();
            episode.setBtnDownloadText("");
            isItem = true;
        }
        if (localName.equalsIgnoreCase(MP3_1) && isItem) {
            episode.setLength(attributes.getValue(LENGTH));
            episode.setAudeoUrl(attributes.getValue(MP3_3));
        }

    }

    /**
     * This will be called when the tags of the XML end.
     **/
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (isItem) {
            if (localName.equalsIgnoreCase(TITLE))
                episode.setTitle(elementValue.toString());
            else if (qName.equalsIgnoreCase(SUMMARY1))
                episode.setSummary(elementValue.toString());
            else if (localName.equalsIgnoreCase(SUMMARY2) && episode.getSummary().isEmpty())
                episode.setSummary(elementValue.toString());
            else if (qName.equalsIgnoreCase(SUMMARY3)
                    && episode.getSummary().equals(""))
                episode.setSummary(elementValue.toString());
            else if (localName.equalsIgnoreCase(MP3_2)
                    && episode.getAudeoUrl().isEmpty())
                episode.setAudeoUrl(elementValue.toString());
            else if (qName.equalsIgnoreCase(DURATION1))
                episode.setDuration(elementValue.toString());
            else if (localName.equalsIgnoreCase(DURATION2)
                    && episode.getDuration().isEmpty())
                episode.setDuration(elementValue.toString());
            else if (localName.equalsIgnoreCase(PUBDATE))
                episode.setDate(elementValue.toString());
        } else {
            if (qName.equalsIgnoreCase(SUMMARY1))
                podcastSummary = elementValue.toString();
            else if (localName.equalsIgnoreCase(SUMMARY2) && podcastSummary.isEmpty())
                podcastSummary = elementValue.toString();
            else if (qName.equalsIgnoreCase(SUMMARY3)
                    && podcastSummary.isEmpty())
                podcastSummary = elementValue.toString();
        }
        if (localName.equals(ITEM_TITLE)) {
            isItem = false;
            allEpisodes.add(episode);
        }
        elementValue = new StringBuilder("");
        elementOn = false;
    }

    /**
     * This is called to get the tags value
     **/

    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (elementOn && isItem || elementOn) {
            elementValue.append(new String(ch, start, length));
        }
    }

    public ArrayList<Episode> getParsedEpisods() {
        return this.allEpisodes;
    }

    public String getPodcastSummary() {
        return podcastSummary;
    }
}
