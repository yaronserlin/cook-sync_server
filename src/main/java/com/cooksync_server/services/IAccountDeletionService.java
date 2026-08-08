package com.cooksync_server.services;

import com.cooksync_server.entities.User;

/**
 * Interface for AccountDeletionService.
 */
public interface IAccountDeletionService {

    void requestDeletion(User user);

    void restoreFromPendingDeletion(User user);

    void purgeExpiredAccounts();
}
