package com.the_tinkering.wk;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.activities.AbstractActivity;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Interface to mark either an AbstractActivity or an AbstractFragment, providing access to
 * some common methods. Yes, the name is a silly joke.
 */
public interface Actment extends LifecycleOwner {
    /**
     * Get the toolbar for this activity or the activity this fragment is attached to.
     *
     * @return the toolbar or null if it doesn't exist (yet).
     */
    @Nullable Toolbar getToolbar();

    /**
     * Jump to another activity immediately.
     *
     * @param clas the class for the destination activity
     */
    void goToActivity(final Class<? extends AbstractActivity> clas);

    /**
     * Jump to the main activity immediately, uses the CLEAR_TOP flag so it
     * clears the backstack history since the last time the main activity was shown.
     */
    void goToMainActivity();

    /**
     * Jump to the subject info for a given subject. Start a new browse activity if needed, or replace the
     * current fragment if we're already on one.
     *
     * @param id the subject ID
     * @param ids the context array of subject IDs, may be empty
     * @param animation the transition animation to use if we're already on the browse activity
     */
    void goToSubjectInfo(long id, List<Long> ids, FragmentTransitionAnimation animation);

    /**
     * Jump to the subject info for a given subject. Start a new browse activity if needed, or replace the
     * current fragment if we're already on one.
     *
     * @param id the subject ID
     * @param ids the context array of subject IDs, may be empty
     * @param animation the transition animation to use if we're already on the browse activity
     */
    void goToSubjectInfo(long id, long[] ids, FragmentTransitionAnimation animation);
}
