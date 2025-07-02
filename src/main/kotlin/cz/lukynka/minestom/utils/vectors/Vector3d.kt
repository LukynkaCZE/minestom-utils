package cz.lukynka.minestom.utils.vectors

import cz.lukynka.minestom.utils.types.Location
import net.minestom.server.instance.Instance
import kotlin.math.pow
import kotlin.math.sqrt

data class Vector3d(var x: Double, var y: Double, var z: Double) {

    constructor() : this(0.0, 0.0, 0.0)
    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(x: Int, y: Double, z: Int) : this(x.toDouble(), y, z.toDouble())
    constructor(x: Double, y: Int, z: Double) : this(x, y.toDouble(), z)
    constructor(single: Double) : this(single, single, single)

    operator fun minus(vector: Vector3d): Vector3d {
        val subVector = this.copy()
        subVector.x -= vector.x
        subVector.y -= vector.y
        subVector.z -= vector.z
        return subVector
    }

    operator fun plus(vector: Vector3d): Vector3d {
        val subVector = this.copy()
        subVector.x += vector.x
        subVector.y += vector.y
        subVector.z += vector.z
        return subVector
    }

    operator fun times(other: Double): Vector3d {
        return times(Vector3d(other))
    }

    operator fun times(vector: Vector3d): Vector3d {
        val subVector = this.copy()
        subVector.x *= vector.x
        subVector.y *= vector.y
        subVector.z *= vector.z
        return subVector
    }

    operator fun timesAssign(vector: Vector3d) {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    operator fun div(vector: Vector3d): Vector3d {
        val subVector = this.copy()
        subVector.x /= vector.x
        subVector.y /= vector.y
        subVector.z /= vector.z
        return subVector
    }

    operator fun divAssign(vector: Vector3d) {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun dot(other: Vector3d): Double = this.x * other.x + this.y * other.y + this.z * other.z

    fun cross(other: Vector3d): Vector3d {
        return Vector3d(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        )
    }

    fun squaredLength(): Double {
        return x * x + y * y + z * z
    }

    fun length(): Double {
        return sqrt(squaredLength())
    }

    fun distance(other: Vector3d): Double = sqrt((this.x - other.x).pow(2.0) + (this.y - other.y).pow(2.0) + (this.z - other.z).pow(2.0))

    fun normalized(): Vector3d {
        val vector = this.copy()
        val magnitude = sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
        return if (magnitude != 0.0) {
            Vector3d(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude)
        } else {
            vector
        }
    }

    val isZero: Boolean get() = x == 0.0 && y == 0.0 && z == 0.0

    fun toLocation(world: Instance): Location = Location(this.x, this.y, this.z, world)
    fun toVector3f() = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    fun toVector3() = Vector3(x.toInt(), y.toInt(), z.toInt())
}
