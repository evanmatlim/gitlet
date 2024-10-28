package gitlet;
/** Driver class for Gitlet, the tiny version-control system.
 *  @author Evan Lim
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        Repository.runItBack(args[0]);
        if (args[0].equals("init")) {
            Repository.init();
        } else if (args[0].equals("add")) {
            Repository.add(args);
        } else if (args[0].equals("commit")) {
            Repository.commit(args);
        } else if (args[0].equals("rm")) {
            Repository.remove(args);
        } else if (args[0].equals("log")) {
            Repository.log();
        } else if (args[0].equals("global-log")) {
            Repository.globalLog();
        } else if (args[0].equals("find")) {
            Repository.find(args);
        } else if (args[0].equals("status")) {
            Repository.status();
        } else if (args[0].equals("checkout")) {
            Repository.checkout(args);
        } else if (args[0].equals("branch")) {
            Repository.branch(args);
        } else if (args[0].equals("rm-branch")) {
            Repository.removeBranch(args);
        } else if (args[0].equals("reset")) {
            Repository.reset(args);
        } else if (args[0].equals("merge")) {
            Repository.merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}
