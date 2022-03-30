cp -rf functions/osync/lib/ functions/osync-data-sync-cron/lib/
cp -rf functions/osync/xyz/ functions/osync-data-sync-cron/xyz/
cp functions/osync/.classpath functions/osync-data-sync-cron/
catalyst deploy
