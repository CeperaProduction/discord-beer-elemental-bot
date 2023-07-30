package me.cepera.discord.bot.beerelemental.repository;

import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.model.RolePermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePermissionRepository {

    Flux<RolePermission> getRolePermissions(long guildId);

    Flux<RolePermission> getRolePermissions(long guildId, long roleId);

    Flux<RolePermission> getRolePermissions(long guildId, Permission permission);

    Mono<RolePermission> getRolePermission(long guildId, long roleId, Permission permission);

    Mono<RolePermission> addRolePermission(RolePermission permission);

    Mono<Void> deleteRolePermission(RolePermission permission);

}
