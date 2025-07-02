package cz.lukynka.minestom.utils.extensions

import cz.lukynka.minestom.utils.types.Location
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance

fun Pos.toLocation(world: Instance): Location {
    return Location(this.x, this.y, this.z, world)
}