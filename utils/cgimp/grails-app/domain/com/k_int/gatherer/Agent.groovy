package com.k_int.gatherer


// A gatherer agent is responsible for turning some custom source
// into a stream of more standard events than can be consumed by aggregators.
// The agent can maintain state information about the remote site (For example
// an OAI from parameter) or can maintain item level data and do checksums on
// a per item basis for services that don't offer resumption

class Agent {

    static constraints = {
    }
}
