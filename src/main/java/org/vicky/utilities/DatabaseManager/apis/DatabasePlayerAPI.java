/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.apis;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

@Path("/players")
public class DatabasePlayerAPI {

  private final DatabasePlayerDAO playerDAO;

  // In a real application, you might inject EntityManager via CDI/Spring.
  public DatabasePlayerAPI() {
    this.playerDAO = new DatabasePlayerDAO();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public DatabasePlayer getPlayer(@PathParam("id") String id) {
    UUID playerId = UUID.fromString(id);
    DatabasePlayer player = playerDAO.findById(playerId);
    if (player == null) {
      throw new NotFoundException("Player not found for id: " + id);
    }
    return player;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void createPlayer(DatabasePlayer player) {
    playerDAO.save(player);
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updatePlayer(@PathParam("id") String id, DatabasePlayer player) {
    // Ensure the ID from the path is set into the entity.
    player.setId(UUID.fromString(id));
    playerDAO.update(player);
  }

  @DELETE
  @Path("/{id}")
  public void deletePlayer(@PathParam("id") String id) {
    playerDAO.delete(UUID.fromString(id));
  }
}
