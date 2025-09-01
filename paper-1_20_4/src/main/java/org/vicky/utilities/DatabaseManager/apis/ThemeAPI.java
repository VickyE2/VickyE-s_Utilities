/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.apis;

import org.vicky.shaded.jakarta.ws.rs.*;
import org.vicky.shaded.jakarta.ws.rs.core.MediaType;
import org.vicky.utilities.DatabaseManager.dao_s.ThemeDAO;
import org.vicky.utilities.DatabaseManager.templates.Theme;

@Path("/themes")
public class ThemeAPI {

    private final ThemeDAO themeDAO;

    // In a real application, you might inject EntityManager via CDI/Spring.
    public ThemeAPI() {
        this.themeDAO = new ThemeDAO();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Theme getTheme(@PathParam("id") String id) {
        Theme theme = themeDAO.findById(id);
        if (theme == null) {
            throw new NotFoundException("Theme not found for id: " + id);
        }
        return theme;
    }

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Theme getThemeByName(@PathParam("name") String name) {
        Theme theme = themeDAO.findByName(name);
        if (theme == null) {
            throw new NotFoundException("Theme not found for name: " + name);
        }
        return theme;
    }

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean doesExistByName(@PathParam("name") String name) {
        Theme theme = themeDAO.findByName(name);
        return theme != null;
    }

    @GET
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean doesExistById(@PathParam("id") String id) {
        Theme theme = themeDAO.findById(id);
        return theme != null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createTheme(Theme theme) {
        themeDAO.save(theme);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTheme(@PathParam("id") String id, Theme theme) {
        theme.setId(id);
        themeDAO.update(theme);
    }

    @DELETE
    @Path("/{id}")
    public void deleteTheme(@PathParam("id") String id) {
        themeDAO.delete(id);
    }
}
