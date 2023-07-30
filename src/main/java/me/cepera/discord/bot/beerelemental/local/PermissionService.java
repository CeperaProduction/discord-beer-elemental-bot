package me.cepera.discord.bot.beerelemental.local;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.model.RolePermission;
import me.cepera.discord.bot.beerelemental.repository.RolePermissionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    @Inject
    public PermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public Flux<RolePermission> getRolePermissions(long guildId){
        return rolePermissionRepository.getRolePermissions(guildId);
    }

    public Flux<Permission> getRolePermissions(long guildId, long roleId){
        return rolePermissionRepository.getRolePermissions(guildId, roleId)
                .map(RolePermission::getPermission);
    }

    public Mono<Boolean> hasRolePermission(long guildId, long roleId, Permission permission){
        return rolePermissionRepository.getRolePermission(guildId, roleId, permission)
                .hasElement();
    }

    public Mono<Void> setRolePermission(long guildId, long roleId, Permission permission, boolean value){
        if(value) {
            return rolePermissionRepository.getRolePermission(guildId, roleId, permission)
                    .switchIfEmpty(Mono.defer(()->{

                        RolePermission perm = new RolePermission();
                        perm.setGuildId(guildId);
                        perm.setRoleId(roleId);
                        perm.setPermission(permission);

                        return rolePermissionRepository.addRolePermission(perm);
                    }))
                    .then();
        }else {
            return rolePermissionRepository.getRolePermission(guildId, roleId, permission)
                    .flatMap(rolePermissionRepository::deleteRolePermission);
        }
    }

    public Mono<Boolean> isAdmin(Guild guild, Member member){
        return internalHasPermission(guild, member, null);
    }

    public Mono<Boolean> hasPermission(Guild guild, Member member, Permission permission){
        Objects.requireNonNull(permission);
        return internalHasPermission(guild, member, permission);
    }

    private Mono<Boolean> internalHasPermission(Guild guild, Member member, @Nullable Permission permission){
        return guild.getOwner()
                .map(m->m.getId().equals(member.getId()))
                .filter(r->r)
                .switchIfEmpty(member.getRoles()
                        .collectList()
                        .flatMap(roles->{
                            if(roles.stream().anyMatch(role->role.getPermissions()
                                    .contains(discord4j.rest.util.Permission.ADMINISTRATOR))) {
                                return Mono.just(true);
                            }
                            if(permission == null) {
                                return Mono.just(false);
                            }
                            Set<Long> roleIds = new HashSet<>();
                            roles.forEach(role->roleIds.add(role.getId().asLong()));
                            return rolePermissionRepository.getRolePermissions(guild.getId().asLong(), permission)
                                    .any(perm->roleIds.contains(perm.getRoleId()));
                        }));
    }

}
