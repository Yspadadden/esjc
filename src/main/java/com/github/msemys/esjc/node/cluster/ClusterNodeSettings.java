package com.github.msemys.esjc.node.cluster;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static com.github.msemys.esjc.util.Numbers.isPositive;
import static com.github.msemys.esjc.util.Preconditions.checkArgument;
import static com.github.msemys.esjc.util.Ranges.ATTEMPTS_RANGE;
import static com.github.msemys.esjc.util.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Cluster node settings.
 */
public class ClusterNodeSettings {

    /**
     * The DNS name to use for discovering endpoints.
     */
    public final String dns;

    /**
     * The maximum number of attempts for discovering endpoints.
     */
    public final int maxDiscoverAttempts;

    /**
     * The interval between discovering endpoint attempts.
     */
    public final Duration discoverAttemptInterval;

    /**
     * The well-known endpoint on which cluster managers are running.
     */
    public final int externalGossipPort;

    /**
     * Endpoints for seeding gossip if not using DNS.
     */
    public final List<GossipSeed> gossipSeeds;

    /**
     * Timeout for cluster gossip.
     */
    public final Duration gossipTimeout;

    private ClusterNodeSettings(Builder builder) {
        dns = builder.dns;
        maxDiscoverAttempts = builder.maxDiscoverAttempts;
        discoverAttemptInterval = builder.discoverAttemptInterval;
        externalGossipPort = builder.externalGossipPort;
        gossipSeeds = builder.gossipSeeds;
        gossipTimeout = builder.gossipTimeout;
    }

    /**
     * Creates a new builder for gossip seed discoverer.
     *
     * @return gossip seed discoverer builder.
     */
    public static BuilderForGossipSeedDiscoverer forGossipSeedDiscoverer() {
        return new BuilderForGossipSeedDiscoverer();
    }

    /**
     * Creates a new builder for DNS discoverer.
     *
     * @return DNS discoverer builder
     */
    public static BuilderForDnsDiscoverer forDnsDiscoverer() {
        return new BuilderForDnsDiscoverer();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClusterNodeSettings{");
        sb.append("dns='").append(dns).append('\'');
        sb.append(", maxDiscoverAttempts=").append(maxDiscoverAttempts);
        sb.append(", discoverAttemptInterval=").append(discoverAttemptInterval);
        sb.append(", externalGossipPort=").append(externalGossipPort);
        sb.append(", gossipSeeds=").append(gossipSeeds);
        sb.append(", gossipTimeout=").append(gossipTimeout);
        sb.append('}');
        return sb.toString();
    }


    /**
     * Builder for gossip seed discoverer
     */
    public static class BuilderForGossipSeedDiscoverer extends Builder {

        /**
         * Sets the maximum number of attempts for discovery (by default, 10 attempts).
         *
         * @param maxDiscoverAttempts the maximum number of attempts for discovery (use {@code -1} for unlimited).
         * @return the builder reference
         */
        public BuilderForGossipSeedDiscoverer maxDiscoverAttempts(int maxDiscoverAttempts) {
            super.maxDiscoverAttempts = maxDiscoverAttempts;
            return this;
        }

        /**
         * Sets the interval between discovering endpoint attempts (by default, 500 milliseconds).
         *
         * @param discoverAttemptInterval the interval between discovering endpoint attempts.
         * @return the builder reference
         */
        public BuilderForGossipSeedDiscoverer discoverAttemptInterval(Duration discoverAttemptInterval) {
            super.discoverAttemptInterval = discoverAttemptInterval;
            return this;
        }

        /**
         * Sets gossip seed endpoints for the client.
         * <p>
         * Note that this should be the external HTTP endpoint of the server, as it is required
         * for the client to exchange gossip with the server. The standard port which should be
         * used here is 2113.
         * </p>
         * <p>
         * If the server requires a specific Host header to be sent as part of the gossip
         * request, use the overload of this method taking {@link GossipSeed} instead.
         * </p>
         *
         * @param endpoints the endpoints of nodes from which to seed gossip.
         * @return the builder reference
         */
        public BuilderForGossipSeedDiscoverer gossipSeedEndpoints(List<InetSocketAddress> endpoints) {
            return gossipSeeds(endpoints.stream().map(GossipSeed::new).collect(toList()));
        }

        /**
         * Sets gossip seed endpoints for the client.
         *
         * @param gossipSeeds the endpoints of nodes from which to seed gossip.
         * @return the builder reference
         */
        public BuilderForGossipSeedDiscoverer gossipSeeds(List<GossipSeed> gossipSeeds) {
            super.gossipSeeds = gossipSeeds;
            return this;
        }

        /**
         * Sets the period after which gossip times out if none is received (by default, 1 second).
         *
         * @param gossipTimeout the period after which gossip times out if none is received.
         * @return the builder reference
         */
        public BuilderForGossipSeedDiscoverer gossipTimeout(Duration gossipTimeout) {
            super.gossipTimeout = gossipTimeout;
            return this;
        }

        /**
         * Builds a cluster node settings.
         *
         * @return cluster node settings
         */
        @Override
        public ClusterNodeSettings build() {
            checkArgument(super.gossipSeeds != null && !super.gossipSeeds.isEmpty(), "Gossip seeds are not specified.");
            return super.build();
        }
    }

    /**
     * Builder for DNS discoverer
     */
    public static class BuilderForDnsDiscoverer extends Builder {

        /**
         * Sets the DNS name under which cluster nodes are listed.
         *
         * @param dns the DNS name under which cluster nodes are listed.
         * @return the builder reference
         */
        public BuilderForDnsDiscoverer dns(String dns) {
            super.dns = dns;
            return this;
        }

        /**
         * Sets the maximum number of attempts for discovery (by default, 10 attempts).
         *
         * @param maxDiscoverAttempts the maximum number of attempts for discovery (use {@code -1} for unlimited).
         * @return the builder reference
         */
        public BuilderForDnsDiscoverer maxDiscoverAttempts(int maxDiscoverAttempts) {
            super.maxDiscoverAttempts = maxDiscoverAttempts;
            return this;
        }

        /**
         * Sets the interval between discovering endpoint attempts (by default, 500 milliseconds).
         *
         * @param discoverAttemptInterval the interval between discovering endpoint attempts.
         * @return the builder reference
         */
        public BuilderForDnsDiscoverer discoverAttemptInterval(Duration discoverAttemptInterval) {
            super.discoverAttemptInterval = discoverAttemptInterval;
            return this;
        }

        /**
         * Sets the well-known port on which the cluster gossip is taking place (by default, 30778 port is used).
         * <p>
         * If you are using the commercial edition of Event Store HA, with Manager nodes in
         * place, this should be the port number of the External HTTP port on which the
         * managers are running.
         * </p>
         * <p>
         * If you are using the open source edition of Event Store HA, this should be the
         * External HTTP port that the nodes are running on. If you cannot use a well-known
         * port for this across all nodes, you can instead use gossip seed discovery and set
         * the endpoint of some seed nodes instead.
         * </p>
         *
         * @param externalGossipPort the cluster gossip port.
         * @return the builder reference
         */
        public BuilderForDnsDiscoverer externalGossipPort(int externalGossipPort) {
            super.externalGossipPort = externalGossipPort;
            return this;
        }

        /**
         * Sets the period after which gossip times out if none is received (by default, 1 second).
         *
         * @param gossipTimeout the period after which gossip times out if none is received.
         * @return the builder reference
         */
        public BuilderForDnsDiscoverer gossipTimeout(Duration gossipTimeout) {
            super.gossipTimeout = gossipTimeout;
            return this;
        }

        /**
         * Builds a cluster node settings.
         *
         * @return cluster node settings
         */
        @Override
        public ClusterNodeSettings build() {
            checkArgument(!isNullOrEmpty(super.dns), "dns is null or empty");

            if (super.externalGossipPort == null) {
                super.externalGossipPort = 30778;
            }

            return super.build();
        }
    }

    /**
     * Base builder
     */
    private static class Builder {
        private String dns;
        private Integer maxDiscoverAttempts;
        private Duration discoverAttemptInterval;
        private Integer externalGossipPort;
        private List<GossipSeed> gossipSeeds;
        private Duration gossipTimeout;

        public ClusterNodeSettings build() {
            if (dns == null) {
                dns = "";
            }

            if (maxDiscoverAttempts == null) {
                maxDiscoverAttempts = 10;
            } else {
                checkArgument(ATTEMPTS_RANGE.contains(maxDiscoverAttempts), "maxDiscoverAttempts value is out of range. Allowed range: %s.", ATTEMPTS_RANGE.toString());
            }

            if (discoverAttemptInterval == null) {
                discoverAttemptInterval = Duration.ofMillis(500);
            }

            if (externalGossipPort == null) {
                externalGossipPort = 0;
            } else {
                checkArgument(isPositive(externalGossipPort), "externalGossipPort should be positive");
            }

            if (gossipSeeds == null) {
                gossipSeeds = emptyList();
            } else {
                gossipSeeds = unmodifiableList(gossipSeeds);
            }

            if (gossipTimeout == null) {
                gossipTimeout = Duration.ofSeconds(1);
            }

            return new ClusterNodeSettings(this);
        }
    }
}
