package cz.lukynka.minestom.utils.extensions

import cz.lukynka.minestom.utils.types.Location
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

fun Instance.locationAt(x: Int, y: Int, z: Int): Location {
    return Location(x, y, z, this)
}

fun Instance.getBlock(location: Location): Block {
    return this.getBlock(location.toMinestom())
}