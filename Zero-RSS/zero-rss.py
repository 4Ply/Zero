#!/bin/python2.7

import feedparser
import json
import os
import re
import requests
import time
from redisworks import Root

bot_chan_url = "https://app2.bot-chan.com/api"
api_key = os.environ['RSS_API_KEY']


root = Root(host='redis', port=6379, db=0)
#root.feeds = [{}]
if root.feeds == None:
    root.feeds = [{}]


def parse_all_known_feeds():
    #print(root.feeds)
    new_feeds = []
    for feed in root.feeds:
        if feed == '' or feed == None or feed == {}:
            continue
        else:
            new_feeds.append(parse_feed(feed))
    if new_feeds != []:
        root.feeds = new_feeds


def parse_feed(feed):
    #print("parse feed: " + str(feed))
    #print("parsed feed items: " + str(feed['parsed_items']))

    items = feedparser.parse(feed['feed_url'])['items']
    for item in items:
        url = item['url'] if 'url' in item else item['link']
        parsed_items = feed['parsed_items'] if 'parsed_items' in feed else []
        if url not in parsed_items:
            parsed_items.append(url)
            consume_feed_item(feed['feed_url'], item, feed['platform_users'])
        feed['parsed_items'] = parsed_items

    return feed


def consume_feed_item(feed_url, item, platform_users):
    for message_id in platform_users:
        #print(str(message_id) + " --- " + item['title'])
        headers = {'content-type': 'application/json'}
        message = "New item '%s' from feed: %s" % (item['title'], feed_url)
        requests.put((bot_chan_url + "/directReply/%s?apikey=%s&message=%s") % (message_id, api_key, message),
                headers=headers)


def check_messages():
    try:
        headers = {'content-type': 'application/json'}
        matchers = ['(?i)watch feed http(|s)://.*', '(?i)(unwatch|remove) feed http(|s)://.*', '(?i)list feeds.*']
        payload = {'matchers': matchers, 'platform': 'RSS'}
        response = requests.post((bot_chan_url + "/uniqueMessages?apikey=%s") % api_key,
                headers=headers,
                data=json.dumps(payload))
        print(response.json())
        for data in response.json():
            data = response.json()[0]

            message = data['message']
            message_id = data['id']
            user_id = get_user_id(data['platformID'])
            print(message)

            if bool(re.match(matchers[0], message)):
                feed_url = message.split(' ')[2]
                print("Feed URL: " + feed_url)
                add_platform_user_to_feed(feed_url, user_id)
                reply_add_success(message_id)
            elif bool(re.match(matchers[1], message)):
                feed_url = message.split(' ')[2]
                print("Feed URL: " + feed_url)
                remove_platform_user_from_feed(feed_url, user_id)
                reply_remove_success(message_id)
            elif bool(re.match(matchers[2], message)):
                reply_with_feed_list(user_id, message_id)
    except Exception as e:
        print(e)
        pass
    except:
        print("Fatal error occured")
        pass


def get_user_id(platform_id):
    headers = {'content-type': 'application/json'}
    response = requests.get((bot_chan_url + "/userID?apikey=%s&platformID=%s&platform=%s") % (api_key, platform_id, 'RSS'),
            headers=headers)
    return response


def reply_add_success(message_id):
    message = "New feed added!"
    reply(message_id, message)


def reply_remove_success(message_id):
    message = "Feed removed from your watchlist"
    reply(message_id, message)


def reply(message_id, message):
    headers = {'content-type': 'application/json'}
    payload = {'originalMessageID': message_id, 'message': message}
    requests.put((bot_chan_url + "/reply?apikey=%s") % api_key,
            headers=headers,
            data=json.dumps(payload))


def add_platform_user_to_feed(feed_url, platform_user):
    feeds = [x for x in root.feeds if x != {}]
    feed_item = None
    for feed_object in feeds:
        if (feed_object['feed_url'] == feed_url):
            feed_item = feed_object

    if feed_item is None:
        feed_item = {'feed_url': feed_url, 'platform_users': [], 'parsed_items': []}
        feeds.append(feed_item)
    if platform_user not in feed_item['platform_users']:
        feed_item['platform_users'].append(platform_user)

    root.feeds = feeds


def remove_platform_user_from_feed(feed_url, platform_user):
    feeds = [x for x in root.feeds if x != {}]
    feed_item = None
    for feed_object in feeds:
        if (feed_object['feed_url'] == feed_url):
            feed_item = feed_object

    if feed_item is None:
        feed_item = {'feed_url': feed_url, 'platform_users': [], 'parsed_items': []}
        feeds.append(feed_item)
    if platform_user in feed_item['platform_users']:
        feed_item['platform_users'].remove(platform_user)
    if feed_item['platform_users'] == []:
        feeds.remove(feed_item)

    root.feeds = feeds


def reply_with_feed_list(platform_id, message_id):
    feeds = [x for x in root.feeds if x != {}]
    feed_urls = []

    for feed_item in feeds:
        print(feed_item)
        if platform_id in feed_item['platform_users']:
            feed_urls.append(feed_item['feed_url'])

    reply(message_id, 'Feeds: %s' % ('\n'.join(feed_urls)))


if __name__ == "__main__":
    i = 0
    while True:
        i = i + 1
        check_messages()
        if i % 5 == 0:
            parse_all_known_feeds()
        if i >= 5:
            i = 0
        time.sleep(1)
