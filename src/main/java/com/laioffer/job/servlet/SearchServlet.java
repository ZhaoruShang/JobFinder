package com.laioffer.job.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.db.MySQLConnection;
import com.laioffer.job.db.RedisConnection;
import com.laioffer.job.entity.Item;
import com.laioffer.job.entity.ResultResponse;
import com.laioffer.job.external.GitHubClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        String userId = request.getParameter("user_id");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));

        MySQLConnection connection = new MySQLConnection();
        Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
        connection.close();
        response.setContentType("application/json");

//         without Redis
//        GitHubClient client = new GitHubClient();
//        List<Item> items = client.search(lat, lon, null);

        // with Redis
        RedisConnection redis = new RedisConnection();
        String cachedResult = redis.getSearchResult(lat, lon, null);

        List<Item> items = null;
        if (cachedResult != null) {
            items = Arrays.asList(mapper.readValue(cachedResult, Item[].class));
        } else {
            GitHubClient client = new GitHubClient();
            items = client.search(lat, lon, null);
            redis.setSearchResult(lat, lon, null, mapper.writeValueAsString(items));
        }
        redis.close();


        for (Item item : items) {
            item.setFavorite(favoritedItemIds.contains(item.getId()));
        }

        mapper.writeValue(response.getWriter(), items);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
