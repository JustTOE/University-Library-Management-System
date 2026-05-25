package dev.tmmc.ulms.objects.dto.request.validation;

/**
 * Bean Validation group for constraints that apply only when creating a resource,
 * not when updating it. Used so a user's password is required on create but may be
 * omitted on update to keep the existing one.
 */
public interface OnCreate {
}
