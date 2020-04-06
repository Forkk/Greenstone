package net.forkk.greenstone.computer

import kotlinx.serialization.Serializable
import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.FileError
import net.forkk.greenstone.grpl.GrplParser
import net.forkk.greenstone.grpl.ListVal
import net.forkk.greenstone.grpl.StringVal
import net.forkk.greenstone.grpl.commands.Command
import net.forkk.greenstone.grpl.commands.CommandGroup

/**
 * Emulates a virtual "filesystem" for an in-game computer.
 *
 * Currently this is just a simple key/value store with no concept of directories or anything like that.
 *
 * Provides a group of GRPL commands for manipulating the filesystem via the `fsCommands` method.
 */
@Serializable
class FileSystem {
    // The root directory
    private var files = hashMapOf<String, ComputerFile>()

    /** List all files on the FS */
    fun listFiles(): Iterable<ComputerFile> = files.values

    /**
     * Removes a file with the given name from the disk.
     */
    fun rmFile(name: String) { files.remove(name) }

    fun mvFile(old: String, new: String) {
        val file = files[old]
        if (file != null) {
            files.remove(old)
        }
    }

    /** Write an entire file from a string. */
    fun writeFile(name: String, content: String) {
        if (name in files) {
            files[name]!!.contents = content
        } else {
            files[name] = ComputerFile(name, content)
        }
    }

    /** Read an entire file into a string */
    fun readFile(name: String): String {
        val file = files[name]
        if (file != null) {
            return file.contents
        } else {
            throw FileError("File $name not found")
        }
    }

    fun fsCommands(be: ComputerBlockEntity): CommandGroup {
        return CommandGroup(
            "fs", "Commands for accessing the filesystem.",
            LSCmd(this), RMCmd(this), MVCmd(this),
            WriteCmd(this), ReadCmd(this),
            EditCmd(this, be), RunCmd(this)
        )
    }

    fun getFile(name: String, create: Boolean = true): ComputerFile {
        val file = files[name]
        if (file != null) {
            return file
        } else if (create) {
            val f = ComputerFile(name)
            files[name] = f
            return f
        } else {
            throw FileError("File $name not found")
        }
    }
}

/**
 * Represents a file in the filesystem.
 */
@Serializable
class ComputerFile(
    val name: String,
    var contents: String = ""
)

// Commands

class LSCmd(private val fs: FileSystem) : Command("ls") {
    override fun exec(ctx: Context) {
        ctx.stack.push(ListVal(fs.listFiles().map { StringVal(it.name) }.toList()))
    }

    override val help: String
        get() = "Pushes a list of all files on the computer."
}

class RMCmd(private val fs: FileSystem) : Command("rm") {
    override fun exec(ctx: Context) {
        val name = ctx.stack.pop().asString()
        fs.rmFile(name)
    }

    override val help: String
        get() = "Pops a string of the stack and deletes the file with the given name.\n" +
                "If the file does not exist, does nothing."
}

class MVCmd(private val fs: FileSystem) : Command("mv") {
    override fun exec(ctx: Context) {
        val new = ctx.stack.pop().asString()
        val old = ctx.stack.pop().asString()
        fs.mvFile(old, new)
    }

    override val help: String
        get() = "Pops two strings, and renames the file specified by the lower string to the name " +
                "specified by the upper one.\n" +
                "Example: `\"foo\" \"bar\" mv` would rename the file \"foo\" to \"bar\"."
}

class ReadCmd(private val fs: FileSystem) : Command("freadall") {
    override fun exec(ctx: Context) {
        val name = ctx.stack.pop().asString()
        ctx.stack.push(StringVal(fs.readFile(name)))
    }

    override val help: String
        get() = "Pops a string of the stack, reads the file with the given name, and " +
                "pushes the contents of the file as a string on the stack."
}

class WriteCmd(private val fs: FileSystem) : Command("fwriteall") {
    override fun exec(ctx: Context) {
        val content = ctx.stack.pop().asString()
        val name = ctx.stack.pop().asString()
        fs.writeFile(name, content)
    }

    override val help: String
        get() = "Pops a filename and a string, and writes the string to the file with the given filename.\n" +
                "If the file already exists, it will be overwritten. If it doesn't exist, it will be " +
                "created."
}

class RunCmd(private val fs: FileSystem) : Command("run") {
    override fun exec(ctx: Context) {
        val name = ctx.stack.pop().asString()
        val prgm = fs.readFile(name)
        ctx.execSync(GrplParser.parse(prgm))
    }

    override val help: String
        get() = "Pops a filename and executes the GRPL program stored in that file."
}

class EditCmd(private val fs: FileSystem, private val be: ComputerBlockEntity) : Command("edit") {
    override fun exec(ctx: Context) {
        val fname = ctx.stack.pop().asString()
        be.startEditing(fname)
    }

    override val help: String
        get() = "Pops a filename and opens the given file for editing in your OS's default text editor.\n" +
                "This is done by copying the file contents from the server to a temporary folder on your " +
                "computer."
}
