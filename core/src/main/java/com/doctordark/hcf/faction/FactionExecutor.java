package com.doctordark.hcf.faction;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.argument.FactionAcceptArgument;
import com.doctordark.hcf.faction.argument.FactionAllyArgument;
import com.doctordark.hcf.faction.argument.FactionAnnouncementArgument;
import com.doctordark.hcf.faction.argument.FactionChatArgument;
import com.doctordark.hcf.faction.argument.FactionClaimArgument;
import com.doctordark.hcf.faction.argument.FactionClaimChunkArgument;
import com.doctordark.hcf.faction.argument.FactionClaimsArgument;
import com.doctordark.hcf.faction.argument.FactionCreateArgument;
import com.doctordark.hcf.faction.argument.FactionDemoteArgument;
import com.doctordark.hcf.faction.argument.FactionDepositArgument;
import com.doctordark.hcf.faction.argument.FactionDisbandArgument;
import com.doctordark.hcf.faction.argument.FactionHelpArgument;
import com.doctordark.hcf.faction.argument.FactionHomeArgument;
import com.doctordark.hcf.faction.argument.FactionInviteArgument;
import com.doctordark.hcf.faction.argument.FactionInvitesArgument;
import com.doctordark.hcf.faction.argument.FactionKickArgument;
import com.doctordark.hcf.faction.argument.FactionLeaderArgument;
import com.doctordark.hcf.faction.argument.FactionLeaveArgument;
import com.doctordark.hcf.faction.argument.FactionListArgument;
import com.doctordark.hcf.faction.argument.FactionMapArgument;
import com.doctordark.hcf.faction.argument.FactionMessageArgument;
import com.doctordark.hcf.faction.argument.FactionOpenArgument;
import com.doctordark.hcf.faction.argument.FactionPromoteArgument;
import com.doctordark.hcf.faction.argument.FactionRenameArgument;
import com.doctordark.hcf.faction.argument.FactionSetHomeArgument;
import com.doctordark.hcf.faction.argument.FactionShowArgument;
import com.doctordark.hcf.faction.argument.FactionStuckArgument;
import com.doctordark.hcf.faction.argument.FactionSubclaimArgumentExecutor;
import com.doctordark.hcf.faction.argument.FactionUnallyArgument;
import com.doctordark.hcf.faction.argument.FactionUnclaimArgument;
import com.doctordark.hcf.faction.argument.FactionUninviteArgument;
import com.doctordark.hcf.faction.argument.FactionUnsubclaimArgument;
import com.doctordark.hcf.faction.argument.FactionWithdrawArgument;
import com.doctordark.hcf.faction.argument.staff.FactionChatSpyArgument;
import com.doctordark.hcf.faction.argument.staff.FactionClaimForArgument;
import com.doctordark.hcf.faction.argument.staff.FactionClearClaimsArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForceDemoteArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForceJoinArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForceKickArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForceLeaderArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForcePromoteArgument;
import com.doctordark.hcf.faction.argument.staff.FactionForceUnclaimHereArgument;
import com.doctordark.hcf.faction.argument.staff.FactionMuteArgument;
import com.doctordark.hcf.faction.argument.staff.FactionRemoveArgument;
import com.doctordark.hcf.faction.argument.staff.FactionSetDeathbanMultiplierArgument;
import com.doctordark.hcf.faction.argument.staff.FactionSetDtrArgument;
import com.doctordark.hcf.faction.argument.staff.FactionSetDtrRegenArgument;
import com.doctordark.util.command.ArgumentExecutor;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        addArgument(new FactionForceUnclaimHereArgument(plugin));
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
        addArgument(new FactionMuteArgument(plugin));
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
