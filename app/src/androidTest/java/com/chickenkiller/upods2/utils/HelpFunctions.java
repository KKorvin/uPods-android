package com.chickenkiller.upods2.utils;

import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.controllers.internet.EpisodesXMLHandler;
import com.chickenkiller.upods2.models.Episode;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Request;

/**
 * Created by Alon Zilberman on 29/03/2016.
 */
public class HelpFunctions {

    public static ArrayList<Episode> parseEpisodes(String url) throws Exception {
        Request episodesRequest = new Request.Builder().url(url).build();
        String response = BackendManager.getInstance().sendSimpleSynchronicRequest(episodesRequest);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        EpisodesXMLHandler episodesXMLHandler = new EpisodesXMLHandler();
        xr.setContentHandler(episodesXMLHandler);
        InputSource inputSource = new InputSource(new StringReader(response));
        xr.parse(inputSource);
        return episodesXMLHandler.getParsedEpisods();
    }
}
