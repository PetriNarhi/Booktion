package com.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
public class SearchController {

    @Value("${omdb.api.key}")

    private String omdbApiKey;
    private final RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query){

        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        System.out.println("Title in search: " + query);
        String googleBooksUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query;
        /////String googleBooksUrl = String.format("https://www.googleapis.com/books/v1/volumes?q=" + "\"%s\" + \"&langRestrict=en\"", q);

        Map<String, Object> book = null;
        try{
            Map response = restTemplate.getForObject(googleBooksUrl, Map.class);
            List items = (List) response.get("items");

            if ( !items.isEmpty() && items != null){
                Map volumeInfo = (Map) ((Map) items.get(0)).get("volumeInfo");
                book = new HashMap<>();

                book.put("title",  volumeInfo.get("title"));
                book.put("authors", volumeInfo.get("authors"));
                book.put("description", volumeInfo.get("description"));
                book.put( "publishedDate", volumeInfo.get("publishedDate"));

                if(volumeInfo.get("imageLinks") != null){
                    Map imageLinks = (Map) volumeInfo.get("imageLinks");
                    book.put("thumbnail", imageLinks.get("thumbnail"));
                }
            }

        }catch (Exception e){}
        result.put("book", book);

        String omdbUrl = "http://www.omdbapi.com/?t=" + query + "&apikey=" + omdbApiKey;
        Map<String, Object> movie = null;
        try{
            Map response = restTemplate.getForObject(omdbUrl, Map.class);
            if ("True".equals(response.get("Response"))){
                movie = new HashMap<>();

                movie.put("title", response.get("Title"));
                movie.put("year", response.get("Year"));
                movie.put("plot", response.get("Plot"));
                movie.put("director", response.get("Director"));
                movie.put("poster", response.get("Poster"));

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        result.put("movie", movie);

        if (book != null && movie != null){
            result.put("analysis", "Match found! Both book and movie exist.");
        }

        System.out.println("Result in search: " + result);
        return result;

    }

}




