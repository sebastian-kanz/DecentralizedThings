
pragma solidity >=0.7.0 <0.9.0;

/** 
 * @title IPFSRootRegistry
 * @dev Simple registry for owned IPFS hashes incl. some metadata.
 */
contract IPFSRootRegistry {
   
   event RootAdded(address indexed owner, string hash);
   event RootRemoved(address indexed owner, string hash);
   
   enum IPFSType { FILE, RAW }
   
   enum AccessLevel { PUBLIC, PRIVATE }
   
   struct IPFSRoot {
        address owner;              // Public key of the owner if the data.
        IPFSType ipfsType;          // Shows what kind of data is stored behind the IPFS hash.
        AccessLevel accessLevel;    // Informs if the data of the ipfs hash is kind of encrypted.
        string rootHash;            // IPFS hash of the data.
        uint timestamp;             // Timestamp of the block when this root was created.
        uint index;                 // Current index of this root in the array of roots in [registeredRoots].
    }

    mapping (address => IPFSRoot[]) private registeredRoots;
    
    /**
     * @dev Adds a root element for the sender of this transaction.
     * @param ipfsType The type of the data behind the new IPFS root hash. 
     * @param accessLevel The access level of the new IPFS root hash indicates if the data is kind of encrypted.
     * @param rootHash The IPFS hash.
     */
    function addRoot(IPFSType ipfsType, AccessLevel accessLevel, string memory rootHash) public {
        IPFSRoot memory newRoot = IPFSRoot({
            owner:  msg.sender,
            ipfsType: ipfsType,
            accessLevel: accessLevel,
            rootHash: rootHash,
            timestamp: block.timestamp,
            index: registeredRoots[msg.sender].length
        });
        registeredRoots[msg.sender].push(newRoot);
        emit RootAdded(msg.sender, rootHash);
    }
    
    /**
     * @dev Removes a root element without keeping the order of the array, therefor the [index] attribute is saved in the struct.
     * @param index The [index] attribute of the to be deleted [IPFSRoot] element.
     */
    function removeRoot(uint index) public {
        require((registeredRoots[msg.sender][index]).owner == msg.sender);
        string memory rootHash = registeredRoots[msg.sender][index].rootHash;
        uint lastRootIndex = registeredRoots[msg.sender].length - 1;
        registeredRoots[msg.sender][index] = registeredRoots[msg.sender][lastRootIndex];
        registeredRoots[msg.sender][index].index = index;
        registeredRoots[msg.sender].pop();
        emit RootRemoved(msg.sender, rootHash);
    }
    
    function getRoots() public view returns (IPFSRoot[] memory){
        return registeredRoots[msg.sender];
    }
    
}