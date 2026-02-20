package org.joinmastodon.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark endpoints that require admin or moderator role.
 * By default, requires ADMIN role. Use moderator=true to allow moderators as well.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
    /**
     * If true, moderators can also access this endpoint.
     * If false (default), only admins can access.
     */
    boolean moderator() default false;
}
