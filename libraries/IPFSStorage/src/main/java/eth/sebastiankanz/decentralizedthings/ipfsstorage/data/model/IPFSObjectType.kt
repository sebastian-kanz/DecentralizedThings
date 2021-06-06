package eth.sebastiankanz.decentralizedthings.ipfsstorage.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class IPFSObjectType {
    @Serializable
    object META : IPFSObjectType()

    @Serializable
    object RAW : IPFSObjectType()

    @Serializable
    object FILE : IPFSObjectType()

    @Serializable
    class ROOT(@SerialName("innerType") val type: IPFSObjectType) : IPFSObjectType()
}