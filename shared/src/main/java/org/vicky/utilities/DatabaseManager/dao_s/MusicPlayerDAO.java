/* Licensed under Apache-2.0 2024-2026. */
package org.vicky.utilities.DatabaseManager.dao_s;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.templates.MusicPlayer;
import org.vicky.utilities.DatabaseManager.templates.MusicPlaylist;
import org.vicky.utilities.DatabaseManager.templates.OwnedPiece;

import java.util.*;

public class MusicPlayerDAO extends GenericDao<MusicPlayer, UUID> {
	public static final MusicPlayerDAO INSTANCE = new MusicPlayerDAO();
	private MusicPlayerDAO() {
	}

	/**
	 * Find a MusicPlayer by its UUID.
	 *
	 * @param id
	 *            the UUID of the player
	 * @return the MusicPlayer instance or null if not found
	 */
	public Optional<MusicPlayer> findById(UUID id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			return Optional.ofNullable(em.find(MusicPlayer.class, id.toString()));
		}
	}

	@Override
	public List<MusicPlayer> getAll() {
		try (EntityManager em = HibernateUtil.getEntityManager()) { // Open new EntityManager
			List<MusicPlayer> pieces = em.createQuery("SELECT t FROM MusicPlayer t", MusicPlayer.class).getResultList();
			em.close();
			return pieces;
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<MusicPlaylist> getPlaylistsFor(UUID id) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			List<MusicPlaylist> playlists = em
					.createQuery("FROM MusicPlaylist WHERE player_id = :id", MusicPlaylist.class).setParameter("id", id)
					.getResultList();
			em.close();
			return playlists;
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	public Integer numberOfOwned(String playerId) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			return em.createQuery("select op from OwnedPiece op where op.player.id = :playerId", MusicPlaylist.class)
					.setParameter("playerId", playerId).getMaxResults();
		} catch (Exception e) {
			return 0;
		}
	}

	public List<OwnedPiece> loadOwnedPieces(String playerId, int page, int amountPerPage, String sorting, String query,
			Map<String, Object> filters) {
		try (EntityManager em = HibernateUtil.getEntityManager()) {
			StringBuilder jpql = new StringBuilder("""
					    select op
					    from OwnedPiece op
					    join fetch op.musicPiece mp
					    where op.player.id = :playerId
					""");

			if (query != null && !query.isBlank()) {
				jpql.append("""
						    and lower(mp.id) like lower(concat('%', :query, '%'))
						""");
			}

			if (filters.containsKey("favorite")) {
				jpql.append(" and op.favorite = :favorite ");
			}
			if (filters.containsKey("genre")) {
				jpql.append(" and op.musicPiece.genre = :genre ");
			}

			if (filters.containsKey("author")) {
				jpql.append(" and :author = ANY(op.musicPiece.authors) ");
			}

			switch (sorting) {
				case "name_ascending" -> jpql.append(" order by mp.id asc ");
				case "name_descending" -> jpql.append(" order by mp.id desc ");
				case "time_ascending" -> jpql.append(" order by op.acquiredAt asc ");
				default -> jpql.append(" order by op.acquiredAt desc ");
			}

			TypedQuery<OwnedPiece> q = em.createQuery(jpql.toString(), OwnedPiece.class);
			q.setParameter("playerId", playerId);

			if (query != null && !query.isBlank()) {
				q.setParameter("query", query.trim());
			}

			if (filters.containsKey("favorite")) {
				q.setParameter("favorite", filters.get("favorite"));
			}
			if (filters.containsKey("genre")) {
				q.setParameter("genre", filters.get("genre"));
			}
			if (filters.containsKey("author")) {
				q.setParameter("author", filters.get("author"));
			}

			q.setFirstResult(page * amountPerPage);
			q.setMaxResults(amountPerPage);

			return q.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}
}
