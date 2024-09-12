CREATE TABLE IF NOT EXISTS File (
	ID integer PRIMARY KEY,
	HostName text NOT NULL,
	Name text,
	OwnerUserID text,
	OwnerGroupID text,
	uuid text -- for DarpaTC data
)

CREATE TABLE IF NOT EXISTS Process (
	ID integer PRIMARY KEY,
	PID integer NOT NULL,
	HostName text NOT NULL,
	ExeName text,
	OwnerUserID text,
	OwnerGroupID text,
	uuid text -- for DarpaTC data
)

CREATE TABLE IF NOT EXISTS Network (
	ID integer PRIMARY KEY,
	HostName text NOT NULL,
	SrcIP cidr,
	SrcPort int,
	DstIP cidr,
	DstPort int,
	uuid text -- for DarpaTC data
)

CREATE TABLE IF NOT EXISTS FileEvent (
	ID integer PRIMARY KEY,
	StartTime bigint,
	EndTime bigint, 
	SrcID integer NOT NULL,
	DstID integer NOT NULL,
	OpType text,
	HostName text NOT NULL,
	Amount bigint,
	EventNo bigint NOT NULL
)

CREATE TABLE IF NOT EXISTS ProcessEvent (
	ID integer PRIMARY KEY,
	StartTime bigint,
	EndTime bigint,
	SrcID integer NOT NULL,
	DstID integer NOT NULL,
	OpType text,
	HostName text NOT NULL,
	EventNo bigint NOT NULL
)

CREATE TABLE IF NOT EXISTS NetworkEvent (
	ID integer PRIMARY KEY,
	StartTime bigint,
	EndTime bigint,
	SrcID integer NOT NULL,
	DstID integer NOT NULL,
	OpType text,
	HostName text NOT NULL,
	Amount bigint,
	EventNo bigint NOT NULL
)

-- Add hostid integer to all the tables for multi-host cases.

