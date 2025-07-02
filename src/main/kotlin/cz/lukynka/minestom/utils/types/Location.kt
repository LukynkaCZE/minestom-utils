package cz.lukynka.minestom.utils.types


import cz.lukynka.minestom.utils.apis.Bound
import cz.lukynka.minestom.utils.extensions.getBlock
import cz.lukynka.minestom.utils.extensions.locationAt
import cz.lukynka.minestom.utils.extensions.toNormalizedVector3f
import cz.lukynka.minestom.utils.truncate
import cz.lukynka.minestom.utils.vectors.Vector2f
import cz.lukynka.minestom.utils.vectors.Vector3
import cz.lukynka.minestom.utils.vectors.Vector3d
import cz.lukynka.minestom.utils.vectors.Vector3f
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction
import kotlin.math.*

data class Location(
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var world: Instance,
) {

    constructor(x: Int, y: Int, z: Int, yaw: Float = 0f, pitch: Float = 0f, world: Instance) :
            this(x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch, world)

    constructor(x: Int, y: Int, z: Int, world: Instance) :
            this(x.toDouble(), y.toDouble(), z.toDouble(), 0f, 0f, world)

    constructor(x: Float, y: Float, z: Float, world: Instance) :
            this(x.toDouble(), y.toDouble(), z.toDouble(), 0f, 0f, world)

    constructor(x: Double, y: Double, z: Double, world: Instance) :
            this(x, y, z, 0f, 0f, world)

    constructor(vector: Vector3, yaw: Float, pitch: Float, world: Instance) :
            this(vector.x, vector.y, vector.z, yaw, pitch, world)

    constructor(vector: Vector3d, yaw: Float, pitch: Float, world: Instance) :
            this(vector.x, vector.y, vector.z, yaw, pitch, world)

    constructor(vector: Vector3f, yaw: Float, pitch: Float, world: Instance) :
            this(vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble(), yaw, pitch, world)

    val blockX: Int get() = floor(x).toInt()
    val blockY: Int get() = floor(y).toInt()
    val blockZ: Int get() = floor(z).toInt()

    val fullX: Int get() = ceil(x).toInt()
    val fullY: Int get() = ceil(y).toInt()
    val fullZ: Int get() = ceil(z).toInt()

    override fun toString(): String =
        "Location(x=${x.truncate(2)}, y=${y.truncate(2)}, z=${z.truncate(2)}, yaw=$yaw, pitch=$pitch, world=${world.dimensionName})"

    fun getNeighbours(): Map<Direction, Location> {
        val blockLocation = getBlockLocation()
        return mapOf(
            Direction.EAST to blockLocation.add(1, 0, 0),
            Direction.WEST to blockLocation.add(-1, 0, 0),
            Direction.UP to blockLocation.add(0, 1, 0),
            Direction.DOWN to blockLocation.add(0, -1, 0),
            Direction.SOUTH to blockLocation.add(0, 0, 1),
            Direction.NORTH to blockLocation.add(0, 0, -1)
        )
    }

    fun getChunk(): Chunk? = world.getChunkAt(this.toMinestom())

    fun relative(direction: Direction): Location {
        return this.add(direction.toNormalizedVector3f())
    }

    fun toMinestom(): Pos {
        return Pos(this.x, this.y, this.z)
    }

    fun add(vector: Vector3f): Location = Location(this.x + vector.x, this.y + vector.y, this.z + vector.z, this.yaw, this.pitch, this.world)
    fun add(x: Int, y: Int, z: Int): Location = Location(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch, this.world)
    fun add(x: Double, y: Double, z: Double): Location = Location(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch, this.world)
    fun add(vector: Vector3): Location = Location(this.x + vector.x, this.y + vector.y, this.z + vector.z, this.yaw, this.pitch, this.world)
    fun add(vector: Vector3d): Location = Location(this.x + vector.x, this.y + vector.y, this.z + vector.z, this.yaw, this.pitch, this.world)
    fun add(location: Location): Location = Location(this.x + location.x, this.y + location.y, this.z + location.z, this.yaw, this.pitch, this.world)

    fun withYawAndPitch(yaw: Float, pitch: Float): Location {
        return Location(x, y, z, yaw, pitch, world)
    }

    fun clone(): Location = Location(this.x, this.y, this.z, this.yaw, this.pitch, this.world)

    fun distance(other: Location): Double = sqrt((this.x - other.x).pow(2.0) + (this.y - other.y).pow(2.0) + (this.z - other.z).pow(2.0))

    fun distanceVector(other: Location): Vector3d {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val distance = sqrt(dx.pow(2.0) + dy.pow(2.0) + dz.pow(2.0))
        return Vector3d(dx / distance, dy / distance, dz / distance)
    }

    fun getBlockLocation(): Location = Location(blockX, blockY, blockZ, world)
    fun getFullLocation(): Location = Location(fullX, fullY, fullZ, world)

    fun getRotation(): Vector2f = Vector2f(yaw, pitch)

    fun getDirection(noPitch: Boolean = false): Vector3d {
        val rotX = yaw
        val rotY = if (noPitch) 0.0 else pitch
        val xz = cos(Math.toRadians(rotY.toDouble()))
        return Vector3d(
            -xz * sin(Math.toRadians(rotX.toDouble())),
            -sin(Math.toRadians(rotY.toDouble())),
            xz * cos(Math.toRadians(rotX.toDouble()))
        )
    }

    fun withLookAt(location: Location): Location {
        if (location == this) return this
        val delta: Vector3d = (this.toVector3d() - this.toVector3d()).normalized()
        return withRotation(
            LocationUtils.getRotationYaw(delta.x, delta.z),
            LocationUtils.getRotationPitch(delta.x, delta.y, delta.z),
        )
    }

    fun withRotation(yaw: Float, pitch: Float): Location {
        return this.clone().apply { this@Location.yaw = yaw; this@Location.pitch = pitch }
    }

    fun withRotation(vector: Vector2f): Location {
        return this.clone().apply { yaw = vector.x; pitch = vector.y }
    }

    fun isSameRotation(yaw: Float, pitch: Float): Boolean {
        return this.yaw.compareTo(yaw) == 0 && this.pitch.compareTo(pitch) == 0
    }

    fun setDirection(vector: Vector3d): Location {
        val loc = this.clone()
        val x = vector.x
        val y = vector.y
        val z = vector.z

        if (x == 0.0 && z == 0.0) {
            loc.yaw = if (y > 0.0) -90.0f else 90.0f
            return loc
        }

        loc.yaw = Math.toDegrees(atan2(-x, z)).toFloat()
        loc.pitch = Math.toDegrees(atan2(-y, sqrt(x * x + z * z))).toFloat()
        return loc
    }

    fun subtract(location: Location): Location = Location(x - location.x, y - location.y, z - location.z, location.pitch, location.yaw, world)
    fun subtract(vector: Vector3f): Location = Location(x - vector.x, y - vector.y, z - vector.z, world)
    fun subtract(vector: Vector3d): Location = Location(x - vector.x, y - vector.y, z - vector.z, world)
    fun subtract(vector: Vector3): Location = Location(x - vector.x, y - vector.y, z - vector.z, world)
    fun subtract(x: Double, y: Double, z: Double): Location = Location(this.x - x, this.y - y, this.z - z, world)
    fun subtract(x: Int, y: Int, z: Int): Location = Location(this.x - x, this.y - y, this.z - z, world)

    fun withNoRotation(): Location = this.clone().apply { yaw = 0f; pitch = 0f }

    val length: Double get() = sqrt(x * x + y * y + z * z)
    val block: Block get() = world.getBlock(this.toMinestom())

    fun toVector3(): Vector3 = Vector3(x.toInt(), y.toInt(), z.toInt())
    fun toVector3f(): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    fun toVector3d(): Vector3d = Vector3d(x, y, z)

    val blockHash: Int get() = (blockX.hashCode() + blockY.hashCode() + blockZ.hashCode() + world.dimensionName.hashCode())
    fun equalsBlock(location: Location): Boolean = this.blockHash == location.blockHash

    fun sameBlock(point: Vector3): Boolean {
        return sameBlock(point.x, point.y, point.z)
    }

    fun sameBlock(blockX: Int, blockY: Int, blockZ: Int): Boolean {
        return this.blockX == blockX && this.blockY == blockY && this.blockZ == blockZ
    }

    fun setBlock(block: Block) {
        world.setBlock(this.toMinestom(), block)
    }

    fun isWithinBound(region: Bound): Boolean {
        val minX = minOf(region.firstLocation.x, region.secondLocation.x)
        val maxX = maxOf(region.firstLocation.x, region.secondLocation.x)
        val minY = minOf(region.firstLocation.y, region.secondLocation.y)
        val maxY = maxOf(region.firstLocation.y, region.secondLocation.y)
        val minZ = minOf(region.firstLocation.z, region.secondLocation.z)
        val maxZ = maxOf(region.firstLocation.z, region.secondLocation.z)

        return x in minX..maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ
    }

    fun getBlocksInRadius(radius: Int): List<Location> {
        val diameter = 2 * radius + 1
        val volume = diameter * diameter * diameter
        val locations = ArrayList<Location>(volume)

        val world = this.world
        val baseX = x
        val baseY = y
        val baseZ = z

        for (dx in -radius..radius) {
            val xCoord = baseX + dx
            for (dy in -radius..radius) {
                val yCoord = baseY + dy
                for (dz in -radius..radius) {
                    val zCoord = baseZ + dz
                    locations.add(Location(xCoord, yCoord, zCoord, world))
                }
            }
        }

        return locations
    }

    fun add(x: Float, y: Float, z: Float): Location {
        return add(Vector3f(x, y, z))
    }

    val closestNonAirBelow: Pair<Block, Location>?
        get() {
            val minY = world.dimensionType.asValue()!!.minY()
            val startY = this.blockY

            for (y in startY downTo minY) {
                val blockLoc = world.locationAt(this.blockX, y, this.blockZ)
                val block = world.getBlock(blockLoc.toMinestom())
                if (!block.isAir) {
                    return block to blockLoc
                }
            }
            return null
        }

    val closestSolidBelow: Pair<Block, Location>?
        get() {
            val minY = world.dimensionType.asValue()!!.minY()
            val startY = this.blockY

            for (y in startY downTo minY) {
                val blockLoc = world.locationAt(this.blockX, y, this.blockZ)
                val block = world.getBlock(blockLoc)
                if (block.isSolid) {
                    return block to blockLoc
                }
            }
            return null
        }

    val closestNonAirAbove: Pair<Block, Location>?
        get() {
            val maxY = world.dimensionType.asValue()!!.height() - 1
            val startY = this.blockY

            for (y in maxY downTo startY) {
                val blockLoc = world.locationAt(this.blockX, y, this.blockZ)
                val block = world.getBlock(blockLoc)
                if (!block.isAir) {
                    return block to blockLoc
                }
            }
            return null
        }

    val closestSolidAbove: Pair<Block, Location>?
        get() {
            val maxY = world.dimensionType.asValue()!!.height() - 1
            val startY = this.blockY

            for (y in maxY downTo startY) {
                val blockLoc = world.locationAt(this.blockX, y, this.blockZ)
                val block = world.getBlock(blockLoc)
                if (block.isSolid) {
                    return block to blockLoc
                }
            }
            return null
        }

}