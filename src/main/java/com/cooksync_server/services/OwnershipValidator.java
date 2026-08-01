package com.cooksync_server.services;

import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;

/**
 * Shared "resource owner or admin" authorization check. Several services
 * (recipes, ingredients, instructions, reviews) gate mutations on the same
 * rule — only the resource's creator or an admin may modify/delete it — so
 * the check is centralized here rather than re-implemented per service.
 */
final class OwnershipValidator {

    private OwnershipValidator() {
    }

    /**
     * @param ownerId the id of the user who owns the resource being acted on
     * @param currentUser the authenticated user attempting the action
     * @param errorMessage message to surface if the user is neither the owner nor an admin
     */
    static void requireOwnerOrAdmin(String ownerId, User currentUser, String errorMessage) {
        if (!ownerId.equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new UnauthorizedActionException(errorMessage);
        }
    }
}
