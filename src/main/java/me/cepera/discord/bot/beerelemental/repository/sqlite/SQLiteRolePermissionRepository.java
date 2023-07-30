package me.cepera.discord.bot.beerelemental.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.model.RolePermission;
import me.cepera.discord.bot.beerelemental.repository.RolePermissionRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SQLiteRolePermissionRepository extends SQLiteRepository implements RolePermissionRepository {

    public SQLiteRolePermissionRepository(SQLiteDatabase database) {
        super(database);
        prepareTables();
    }

    private void prepareTables() {
        connect(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS role_permissions ("
                    + "id integer PRIMARY KEY, "
                    + "guildId integer NOT NULL, "
                    + "roleId integer NOT NULL, "
                    + "permission text NOT NULL)"));
    }


    @Override
    public Flux<RolePermission> getRolePermissions(long guildId) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, roleId, permission FROM role_permissions WHERE guildId = ?");
            stm.setLong(1, guildId);

            ResultSet rs = stm.executeQuery();

            List<RolePermission> permissions = new ArrayList<>();

            while(rs.next()) {

                RolePermission perm = new RolePermission();
                perm.setId(rs.getInt(1));
                perm.setGuildId(guildId);
                perm.setRoleId(rs.getLong(2));
                perm.setPermission(Permission.fromValue(rs.getString(3)));

                if(perm.getPermission() != null) {
                    permissions.add(perm);
                }

            }

            return permissions;

        })));
    }

    @Override
    public Flux<RolePermission> getRolePermissions(long guildId, long roleId) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, permission FROM role_permissions WHERE guildId = ? AND roleId = ?");
            stm.setLong(1, guildId);
            stm.setLong(2, roleId);

            ResultSet rs = stm.executeQuery();

            List<RolePermission> permissions = new ArrayList<>();

            while(rs.next()) {

                RolePermission perm = new RolePermission();
                perm.setId(rs.getInt(1));
                perm.setGuildId(guildId);
                perm.setRoleId(roleId);
                perm.setPermission(Permission.fromValue(rs.getString(2)));

                if(perm.getPermission() != null) {
                    permissions.add(perm);
                }

            }

            return permissions;

        })));
    }

    @Override
    public Flux<RolePermission> getRolePermissions(long guildId, Permission permission) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, roleId FROM role_permissions WHERE guildId = ? AND permission = ?");
            stm.setLong(1, guildId);
            stm.setString(2, permission.getValue());

            ResultSet rs = stm.executeQuery();

            List<RolePermission> permissions = new ArrayList<>();

            while(rs.next()) {

                RolePermission perm = new RolePermission();
                perm.setId(rs.getInt(1));
                perm.setGuildId(guildId);
                perm.setRoleId(rs.getLong(2));
                perm.setPermission(permission);

                permissions.add(perm);

            }

            return permissions;

        })));
    }

    @Override
    public Mono<RolePermission> getRolePermission(long guildId, long roleId, Permission permission) {
        return Mono.fromSupplier(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id FROM role_permissions WHERE guildId = ? AND roleId = ? AND permission = ?");
            stm.setLong(1, guildId);
            stm.setLong(2, roleId);
            stm.setString(3, permission.getValue());

            ResultSet rs = stm.executeQuery();

            if(rs.next()) {

                RolePermission perm = new RolePermission();
                perm.setId(rs.getInt(1));
                perm.setGuildId(guildId);
                perm.setRoleId(roleId);
                perm.setPermission(permission);

                return perm;

            }

            return null;

        }));
    }

    @Override
    public Mono<RolePermission> addRolePermission(RolePermission permission) {
        return Mono.fromSupplier(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("INSERT INTO role_permissions (guildId, roleId, permission) VALUES (?,?,?)");
            stm.setLong(1, permission.getGuildId());
            stm.setLong(2, permission.getRoleId());
            stm.setString(3, permission.getPermission().getValue());

            stm.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT last_insert_rowid()");

            if(rs.next()) {
                permission.setId(rs.getInt(1));
            }

            return permission;
        }));
    }

    @Override
    public Mono<Void> deleteRolePermission(RolePermission permission) {
        if(permission.getId() == null) {
            return Mono.empty();
        }
        return Mono.fromRunnable(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("DELETE FROM role_permissions WHERE id = ?");
            stm.setInt(1, permission.getId());
            stm.executeUpdate();

            return null;
        }));
    }

}
