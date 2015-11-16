package org.ipvp.hcf.faction;

import com.doctordark.util.command.ArgumentExecutor;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.argument.FactionAcceptArgument;
import org.ipvp.hcf.faction.argument.FactionAllyArgument;
import org.ipvp.hcf.faction.argument.FactionAnnouncementArgument;
import org.ipvp.hcf.faction.argument.FactionChatArgument;
import org.ipvp.hcf.faction.argument.FactionClaimArgument;
import org.ipvp.hcf.faction.argument.FactionClaimChunkArgument;
import org.ipvp.hcf.faction.argument.FactionClaimsArgument;
import org.ipvp.hcf.faction.argument.FactionCreateArgument;
import org.ipvp.hcf.faction.argument.FactionDemoteArgument;
import org.ipvp.hcf.faction.argument.FactionDepositArgument;
import org.ipvp.hcf.faction.argument.FactionDisbandArgument;
import org.ipvp.hcf.faction.argument.FactionHelpArgument;
import org.ipvp.hcf.faction.argument.FactionHomeArgument;
import org.ipvp.hcf.faction.argument.FactionInviteArgument;
import org.ipvp.hcf.faction.argument.FactionInvitesArgument;
import org.ipvp.hcf.faction.argument.FactionKickArgument;
import org.ipvp.hcf.faction.argument.FactionLeaderArgument;
import org.ipvp.hcf.faction.argument.FactionLeaveArgument;
import org.ipvp.hcf.faction.argument.FactionListArgument;
import org.ipvp.hcf.faction.argument.FactionMapArgument;
import org.ipvp.hcf.faction.argument.FactionMessageArgument;
import org.ipvp.hcf.faction.argument.FactionOpenArgument;
import org.ipvp.hcf.faction.argument.FactionPromoteArgument;
import org.ipvp.hcf.faction.argument.FactionRenameArgument;
import org.ipvp.hcf.faction.argument.FactionSetHomeArgument;
import org.ipvp.hcf.faction.argument.FactionShowArgument;
import org.ipvp.hcf.faction.argument.FactionStuckArgument;
import org.ipvp.hcf.faction.argument.FactionSubclaimArgumentExecutor;
import org.ipvp.hcf.faction.argument.FactionUnallyArgument;
import org.ipvp.hcf.faction.argument.FactionUnclaimArgument;
import org.ipvp.hcf.faction.argument.FactionUninviteArgument;
import org.ipvp.hcf.faction.argument.FactionUnsubclaimArgument;
import org.ipvp.hcf.faction.argument.FactionWithdrawArgument;
import org.ipvp.hcf.faction.argument.staff.FactionChatSpyArgument;
import org.ipvp.hcf.faction.argument.staff.FactionClaimForArgument;
import org.ipvp.hcf.faction.argument.staff.FactionClearClaimsArgument;
import org.ipvp.hcf.faction.argument.staff.FactionForceDemoteArgument;
import org.ipvp.hcf.faction.argument.staff.FactionForceJoinArgument;
import org.ipvp.hcf.faction.argument.staff.FactionForceKickArgument;
import org.ipvp.hcf.faction.argument.staff.FactionForceLeaderArgument;
import org.ipvp.hcf.faction.argument.staff.FactionForcePromoteArgument;
import org.ipvp.hcf.faction.argument.staff.FactionRemoveArgument;
import org.ipvp.hcf.faction.argument.staff.FactionSetDeathbanMultiplierArgument;
import org.ipvp.hcf.faction.argument.staff.FactionSetDtrArgument;
import org.ipvp.hcf.faction.argument.staff.FactionSetDtrRegenArgument;

/**
 * Class to handle the command and tab completion for the faction command.
 */
public class FactionExecutor extends ArgumentExecutor {

    private final CommandArgument helpArgument;

    public FactionExecutor(HCF plugin) {
        super("faction");

        addArgument(new FactionAcceptArgument(plugin));
        addArgument(new FactionAllyArgument(plugin));
        addArgument(new FactionAnnouncementArgument(plugin));
        addArgument(new FactionChatArgument(plugin));
        addArgument(new FactionChatSpyArgument(plugin));
        addArgument(new FactionClaimArgument(plugin));
        addArgument(new FactionClaimChunkArgument(plugin));
        addArgument(new FactionClaimForArgument(plugin));
        addArgument(new FactionClaimsArgument(plugin));
        addArgument(new FactionClearClaimsArgument(plugin));
        addArgument(new FactionCreateArgument(plugin));
        addArgument(new FactionDemoteArgument(plugin));
        addArgument(new FactionDepositArgument(plugin));
        addArgument(new FactionDisbandArgument(plugin));
        addArgument(new FactionSetDtrRegenArgument(plugin));
        addArgument(new FactionForceDemoteArgument(plugin));
        addArgument(new FactionForceJoinArgument(plugin));
        addArgument(new FactionForceKickArgument(plugin));
        addArgument(new FactionForceLeaderArgument(plugin));
        addArgument(new FactionForcePromoteArgument(plugin));
        addArgument(helpArgument = new FactionHelpArgument(this));
        addArgument(new FactionHomeArgument(this, plugin));
        addArgument(new FactionInviteArgument(plugin));
        addArgument(new FactionInvitesArgument(plugin));
        addArgument(new FactionKickArgument(plugin));
        addArgument(new FactionLeaderArgument(plugin));
        addArgument(new FactionLeaveArgument(plugin));
        addArgument(new FactionListArgument(plugin));
        addArgument(new FactionMapArgument(plugin));
        addArgument(new FactionMessageArgument(plugin));
        addArgument(new FactionOpenArgument(plugin));
        addArgument(new FactionRemoveArgument(plugin));
        addArgument(new FactionRenameArgument(plugin));
        addArgument(new FactionPromoteArgument(plugin));
        addArgument(new FactionSetDtrArgument(plugin));
        addArgument(new FactionSetDeathbanMultiplierArgument(plugin));
        addArgument(new FactionSetHomeArgument(plugin));
        addArgument(new FactionShowArgument(plugin));
        addArgument(new FactionStuckArgument(plugin));
        addArgument(new FactionSubclaimArgumentExecutor(plugin));
        addArgument(new FactionUnclaimArgument(plugin));
        addArgument(new FactionUnallyArgument(plugin));
        addArgument(new FactionUninviteArgument(plugin));
        addArgument(new FactionUnsubclaimArgument(plugin));
        addArgument(new FactionWithdrawArgument(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            helpArgument.onCommand(sender, command, label, args);
            return true;
        }

        CommandArgument argument = getArgument(args[0]);
        if (argument != null) {
            String permission = argument.getPermission();
            if (permission == null || sender.hasPermission(permission)) {
                argument.onCommand(sender, command, label, args);
                return true;
            }
        }

        helpArgument.onCommand(sender, command, label, args);
        return true;
    }
}
