package net.ohauge.story.entity;

import net.ohauge.story.entity.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "storyendpoint", namespace = @ApiNamespace(ownerDomain = "ohauge.net", ownerName = "ohauge.net", packagePath = "story.entity") )
public class StoryEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listStory")
	public CollectionResponse<Story> listStory(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Story> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Story.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Story>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Story obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Story> builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getStory")
	public Story getStory(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Story story = null;
		try {
			story = mgr.getObjectById(Story.class, id);
		} finally {
			mgr.close();
		}
		return story;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param story the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertStory")
	public Story insertStory(Story story) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			
			if (story.getId() != null && containsStory(story)) {
				throw new EntityExistsException("Object already exists");
			}
			
			mgr.makePersistent(story);
		} finally {
			mgr.close();
		}
		return story;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param story the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateStory")
	public Story updateStory(Story story) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsStory(story)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(story);
		} finally {
			mgr.close();
		}
		return story;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeStory")
	public void removeStory(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Story story = mgr.getObjectById(Story.class, id);
			mgr.deletePersistent(story);
		} finally {
			mgr.close();
		}
	}

	private boolean containsStory(Story story) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Story.class, story.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
