const dmap = require('./dmap.js')

module.exports = utils = {}

const makeRPC = async (url, method, params) => {
    console.log("js,makeRPC:", url, method, params)
    let result = null
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                "jsonrpc": "2.0",
                "method": method,
                "params": params,
                "id": 0
            }),
        });
        ({ result } = await response.json())
    }
    catch (err) { return err }
    console.log("js,makeRPC:", url, method, params, "=>", result)
    return result
}

const RPCGetStorage = async (url, address, slot) => {
    const block = await makeRPC(url, "eth_blockNumber", [])
    return await makeRPC(url, "eth_getStorageAt", [address, slot, block])
}

const windowGetStorage = async (address, slot) => {
    const block = await window.ethereum.request({ method: 'eth_blockNumber', params: [] });
    return await window.ethereum.request({ method: 'eth_getStorageAt', params: [address, slot, block] });
}

const getFacade = async (customURL) => {
    let storageFunction = null, description = ''

    if (await makeRPC(customURL, "eth_chainId", [])) {
        storageFunction = RPCGetStorage.bind(null, customURL)
        description = 'custom node'
    } else if (typeof window.ethereum !== 'undefined' &&
        await window.ethereum.request({ method: 'eth_chainId', params: [] })) {
        storageFunction = windowGetStorage
        description = 'window.ethereum'
    } else {
        throw 'no ethereum connection'
    }
    return [{
        provider: { getStorageAt: storageFunction },
        address: dmap.address
    }, description]
}

utils.RPCGetStorage = RPCGetStorage

utils.makeRPC = makeRPC

utils.getFacade = getFacade