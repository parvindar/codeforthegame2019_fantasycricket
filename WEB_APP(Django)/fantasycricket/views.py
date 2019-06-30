from django.shortcuts import render, redirect
from django.http import HttpResponse
#import pyrebase
from .forms import *
import firebase_admin
from firebase_admin import credentials, firestore
from paytm import Checksum
import random
from django.views.decorators.csrf import csrf_exempt
import urllib
import json
import random
from datetime import datetime,timedelta
import inflect # plurazing words

# Create your views here.

##### PAYTM INTEGRATION
MERCHANT_KEY_PAYTM = 'GSjLXPVD@2SvsmhR'

##### FIREBASE FIRESTORE DATABASE CONNECT
cred = credentials.Certificate("fantasycricket-af35d-firebase-adminsdk-rgwxo-9287d56828.json")
default_app = firebase_admin.initialize_app(cred)
db = firestore.client()

##### CRICKET API INTEGRATION
CRICKET_API = db.collection('Projectinfo').document('cricket-api').get().get('API_KEY_WEB')

############################ FIND SCORES #############

def isAdmin(user):
    if str(user.get('UserType'))=='admin':
        return True
    return False

def BattingScore(score):
    runs = float(score['R'])
    bowls = float(score['B'])
    fours = float(score['4s'])
    sixes = float(score['6s'])
    strike_rate = float(score['SR'])
    
    points = 0.0
    
    points += ((runs*0.5) + (fours*0.5) + (sixes) + (int(runs/50)*2) + (int(runs/100)*2))
    if(strike_rate<40):
        points -= 3
    elif strike_rate<50:
        points -= 2
    elif strike_rate<60:
        points -= 1
    elif strike_rate>80:
        points += 1

    return points


def BowlingScore(score):
    points = 0.0
    
    wickets = float(score['W'])
    overs = float(score['O'])
    maidens = float(score['M'])
    runs = float(score['R'])
    econ = float(score['Econ'])
    dots = float(score['0s'])
    
    points += ((wickets*12) + (int(wickets/4))*2 + (int(wickets/5)*2) + maidens*2 + dots*0.5)
    if(overs>5):
        if(econ>9):
            points -= 3
        elif econ>8:
            points -= 2
        elif econ>7:
            points -= 1
        elif econ>=3.5 and econ<4.5:
            points += 1
        elif econ>=2.5 and econ<3.5:
            points += 2
        elif econ<2.5:
            points += 3
    return points


def FieldingScore(score):
    points = 0.0
    
    LBW = score['lbw']
    catches = score['catch']
    runouts = score['runout']
    stumped = score['stumped']
    bowled = score['bowled']

    points += ((catches*4) + (LBW/2) + (stumped*6) + (bowled*6))
    
    return points
    
######################################################


    
def FAQ(request):
    return render(request, 'FAQ.html', {})

def AboutUs(request):
    return render(request, 'AboutUs.html', {})

def reg(request):
    form = RegForm()
    if request.method=='POST':
        # user exists with username
        if db.collection('Users').document(request.POST['username']).get().to_dict():
            print('\n\ngot')
            error='This Username already exists'
            return render(request, 'register.html', {'form': form, 'error': error})

        newData = {
            'Name': request.POST['name'],
            'Email': request.POST['email'],
            'Username': request.POST['username'],
            'Password': request.POST['password'],
            'UserType': 'user',
            'Winnings': float(0.0),
            'xp': float(0.0),
            'Cash': float(0.0),
            'Username_insensitive': (request.POST['username']).lower(),
            'Name_insensitive': (request.POST['name']).lower(),
        }
        db.collection('Users').document(request.POST['username']).set(newData)
        print('\nnew user created successfully\n')
        return redirect('fantasycricket:login')
    return render(request, 'register.html', {'form': form})


def login(request):
    form = LoginForm()
    if request.method=='POST':
        print(request.POST)
        print(db.collection('Users').document(request.POST['username']).get().get('Password'))
        userDoc = db.collection('Users').document(request.POST['username']).get()
        if request.POST['password'] == userDoc.get('Password'):
            print('\nmatched\n')
            user = userDoc
            request.session['user_id'] = user.get('Username')
            return redirect('fantasycricket:home')
        else:
            error = "Username and password didn't match"
            return render(request, 'login.html', {'form': form, 'error': error})
    return render(request, 'login.html', {'form': form})

def home(request):
    log = False
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        username = user.get('Username')
        admin = isAdmin(user)
        log = True
        return render(request, 'home.html', {'log': log, 'admin': admin, 'username': username})
    return render(request, 'home.html', {'log': log})
    
def logout(request):
    if 'user_id' in request.session:
        del request.session['user_id']
        print('deleted\n\n')
    return render(request, 'logout.html', {})

def profile(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        name = user.get('Name')
        username = user.get('Username')
        email = user.get('Email')
        winnings = user.get('Winnings')
        xp = user.get('xp')
        cash = user.get('Cash')
        admin = isAdmin(user)
        return render(request, 'profile.html', {'name': name, 'username': username, 'email': email, 'winnings': winnings, 'xp': xp, 'cash': cash, 'admin': admin})
    return redirect('fantasycricket:login')


def matches(request):
    if 'choice_contest_id' in request.session:
        print('found contest id....')
        del request.session['choice_contest_id']
        print('............deleted')
    if 'choice_match_id' in request.session:
        print('found match id....')
        del request.session['choice_match_id']
        print('............deleted')
        
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        newMatches = urllib.request.urlopen('https://cricapi.com/api/matches?apikey=' + str(CRICKET_API))
        json_data_new = json.loads(newMatches.read().decode(newMatches.info().get_param('charset') or 'utf-8'))
        newMatches = []
        for x in json_data_new['matches']:
            if x['type']=='ODI':
                newMatch = db.collection('Matches').document(str(x['unique_id'])).collection('Contests').document('megaContest'+str(x['unique_id']))
                doc = newMatch.get()
                doc_dict = doc.to_dict()
                if doc_dict==None:
                    print('creating new contests')
                    megaContest = {
                        'ContestName': 'Mega Contest',
                        'Finished': False,
                        'Price': '6',
                        'SpotsFilled': 0,
                        'TotalPrize': '10000',
                        'TotalSpots': '2000',
                    }
                    beginnersContest = {
                        'ContestName': 'Beginners Contest',
                        'Finished': False,
                        'Price': '6',
                        'SpotsFilled': 0,
                        'TotalPrize': '1000',
                        'TotalSpots': '200',
                    }
                    championsContest = {
                        'ContestName': 'Champions Contest',
                        'Finished': False,
                        'Price': '30',
                        'SpotsFilled': 0,
                        'TotalPrize': '5000',
                        'TotalSpots': '200',
                    }
                    print('\nadding\n')
                    db.collection('Matches').document(str(x['unique_id'])).collection('Contests').document('megaContest'+str(x['unique_id'])).set(megaContest)
                    db.collection('Matches').document(str(x['unique_id'])).collection('Contests').document('beginnersContest'+str(x['unique_id'])).set(beginnersContest)
                    db.collection('Matches').document(str(x['unique_id'])).collection('Contests').document('championsContest'+str(x['unique_id'])).set(championsContest)
                    print('\nadded\n')
                
                ## check if match timing is > 3days
                x['applicable'] = ((datetime.now()+timedelta(days=3)) > datetime.strptime(x['dateTimeGMT'][2:19], '%y-%m-%dT%H:%M:%S'))
                
                if (datetime.strptime(x['dateTimeGMT'][2:19], '%y-%m-%dT%H:%M:%S')-datetime.now() > timedelta(days=1)):
                    p = inflect.engine()
                    rem = (datetime.strptime(x['dateTimeGMT'][2:19], '%y-%m-%dT%H:%M:%S')-datetime.now()).days
                    x['remainingTime'] = str(rem) + ' ' + p.plural('day', rem) + ' remaining'
                else:
                    x['remainingTime'] = str(int(((datetime.strptime(x['dateTimeGMT'][2:19], '%y-%m-%dT%H:%M:%S')-datetime.now()).seconds)/3600)) + ' hours remaining'
                
                newMatches.append(x)
        return render(request, 'matches.html', {'matches': newMatches})
    return redirect('fantasycricket:login')
    

def contests(request):
    if 'user_id' in request.session:
        toShow = []
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        allContests = None
        if request.method=='POST':
            print(request.POST)
            if 'choice_match_id' in request.POST:
                request.session['choice_match_id'] = request.POST['choice_match_id']
                allContests = db.collection('Matches').document(str(request.POST['choice_match_id'])).collection('Contests').get()
                for contest in allContests:
                    print(str(contest.id) + ' : ' + str(contest.to_dict()))
                return redirect('fantasycricket:contests')
            
            if 'createContest' in request.POST:
                print('\n\nohohoohohohoh\n\n\n\n')
                newContest = {
                    'ContestName': request.POST['contestName'],
                    'Finished': False,
                    'Price': str(request.POST['price']),
                    'SpotsFilled': 0,
                    'TotalPrize': str(request.POST['prize']),
                    'TotalSpots': str(request.POST['totSpots']),
                    'Type': str(request.POST['contestType']),
                }
                db.collection('Matches').document(str(request.session['choice_match_id'])).collection('Contests').document('shortContest'+str(username)+str(random.randint(100000, 999999))).set(newContest)
                return redirect('fantasycricket:contests')            
            
        elif request.method=='GET':
            print('get')
            allContests = db.collection('Matches').document(str(request.session['choice_match_id'])).collection('Contests').get()
            for contest in allContests:
                print(str(contest.id) + ' : ' + str(contest.to_dict()))
                newDict = contest.to_dict()
                newDict['id'] = contest.id
                toShow.append(newDict)
        return render(request, 'contests.html', {'contests': toShow})
    return redirect('fantasycricket:login')

def contestSelectPlayers(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        newPlayers = urllib.request.urlopen('https://cricapi.com/api/fantasySquad?apikey=' + CRICKET_API + '&unique_id='+str(request.session['choice_match_id']))
        json_data_players = json.loads(newPlayers.read().decode(newPlayers.info().get_param('charset') or 'utf-8'))
        #print(str(json_data_players) + '\n\n')
        t1 = json_data_players['squad'][0]['name']
        t2 = json_data_players['squad'][1]['name']
        players1 = json_data_players['squad'][0]['players']
        players2 = json_data_players['squad'][1]['players']
        
        for player in players1:
            team = db.collection('Team').document(str(t1)).get()
            if str(team.get('captain'))==str(player['name']):
                player['credit'] = 15.0
            elif str(team.get('man_of_the_match'))==str(player['name']):
                player['credit'] = 11.0
            elif str(team.get('vice_captain'))==str(player['name']):
                player['credit'] = 10.0
            else:
                player['credit'] = 8.0
                
        for player in players2:
            team = db.collection('Team').document(str(t2)).get()
            if str(team.get('captain'))==str(player['name']):
                player['credit'] = 15.0
            elif str(team.get('man_of_the_match'))==str(player['name']):
                player['credit'] = 11.0
            elif str(team.get('vice_captain'))==str(player['name']):
                player['credit'] = 10.0
            else:
                player['credit'] = 8.0
            
        if request.method=='POST':
            print('post data : ' + str(request.POST))
            
            if 'choice_contest_id' in request.POST:
                ## activate session and go to get method
                request.session['choice_contest_id'] = request.POST['choice_contest_id']
                return redirect('fantasycricket:contestSelectPlayers')
            
            if 'choice_confirmed' in request.POST:                
                contest = db.collection('Matches').document(str(request.session['choice_match_id'])).collection('Contests').document(str(request.session['choice_contest_id']))
                match = db.collection('Matches').document(str(request.session['choice_match_id'])).get()
                
                ## withdraw cash from user wallet
                cash = float(user.get().get('Cash'))-float(contest.get().get('Price'))
                if(cash<0):
                    return render(request, 'contestSelectPlayers.html', {'t1': t1, 't2': t2, 'players1': players1, 'players2': players2, 'error': "You don't have enough cash in your wallet, please add money to continue."})
                newCash = {
                    'Cash': cash,
                }
                user.update(newCash)
                print('\ncash done\n')
                
                ## Update spots of the contest
                spots = contest.get().get('SpotsFilled') + 1
                newSpots = {
                    'SpotsFilled': spots,
                }
                contest.update(newSpots)
                print('\nspots done\n')
                
                ## In specific contest, Add User
                team = []
                for x in request.POST.getlist('players'):
                    print('\nappending\n')
                    print('x = ' + str(x))
                    arr = x.split(',')
                    print('arr = ' + str(arr))
                    newDict = {
                        'credits': 9,
                        'name': str(arr[0]),
                        'pid': str(arr[1]),
                        'points': 0,
                        'team': str(arr[2]),
                    }
                    if x==request.POST['captain']:
                        newDict['captain'] = True
                    elif x==request.POST['v_captain']:
                        newDict['vicecaptain'] = True
                    team.append(newDict)
                    print('appended\n')
                loadData = {
                    'Points': 0,
                    'Team': team,
                    'Finished': False,
                }
                contest.collection('Participants').document(str(username)).set(loadData)
                
                # In specific User, add Match
                doc = user.collection('Matches').document(request.session['choice_match_id'])
                cts = doc.get().get('contests')
                if cts:
                    cts.append(str(request.session['choice_contest_id']))
                else:
                    cts = [str(request.session['choice_contest_id'])]
                newDict = doc.get().to_dict()
                
                tempDict = {
                    'ParticipantID': request.session['user_id'],
                    'TotalPoints': 0,
                }
                toAdd = {
                    'contests': cts,
                    request.session['choice_contest_id']: tempDict,
                }
                if len(cts)==1: ## first time adding => set
                    user.collection('Matches').document(request.session['choice_match_id']).set(toAdd)
                else: ## not first time => update
                    user.collection('Matches').document(request.session['choice_match_id']).update(toAdd)
                
                print('all done\n\n')
                return redirect('fantasycricket:myMatches')
                
        elif request.method=='GET':
            contest = db.collection('Matches').document(str(request.session['choice_match_id'])).collection('Contests').document(str(request.session['choice_contest_id']))
            match = db.collection('Matches').document(str(request.session['choice_match_id'])).get()

        return render(request, 'contestSelectPlayers.html', {'t1': t1, 't2': t2, 'players1': players1, 'players2': players2})
    return redirect('fantasycricket:login')


def myMatches(request):
    if 'choice_match_id_user' in request.session:
        del request.session['choice_match_id_user']
    
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        
        newMatches = urllib.request.urlopen('https://cricapi.com/api/matches?apikey=' + str(CRICKET_API))
        json_data_new = json.loads(newMatches.read().decode(newMatches.info().get_param('charset') or 'utf-8'))
        myMatches = []
        all_matches_id = user.collection('Matches').get()
        #for x in all_matches_id:
        #    print(x.id)
        print('\n\n')
        for x in all_matches_id:
            curr_id = x.id
            for y in json_data_new['matches']:
                if (str(y['unique_id'])==str(curr_id)):
                    print('appended')
                    myMatches.append(y)
        print(myMatches)
        return render(request, 'user/matches.html', {'myMatches': myMatches})
    return redirect('fantasycricket:login')


def myContests(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        
        if request.method=='POST':
            if 'choice_match_id_user' in request.POST:
                request.session['choice_match_id_user'] = request.POST['choice_match_id_user']
                return redirect('fantasycricket:myContests')
            else:
                return redirect('fantasycricket:myMatches')
            
        if request.method=='GET':
            print('get')
            match = user.collection('Matches').document(request.session['choice_match_id_user'])
            matchInMatches = db.collection('Matches').document(request.session['choice_match_id_user'])
            contests = []
            for c in match.get().get('contests'):
                print(c)
                newContest = matchInMatches.collection('Contests').document(str(c)).get().to_dict()
                newContest['id'] = str(c)
                contests.append(newContest)
            print(contests)
            return render(request, 'user/contests.html', {'contests': contests})
    return redirect('fantasycricket:login')
        

def myContestStatus(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        players = []
        
        if request.method=='POST':
            #print(request.POST)
            if 'choice_contest_id' in request.POST:
                request.session['choice_contest_id_user'] = request.POST['choice_contest_id']
                return redirect('fantasycricket:myContestStatus')
        if request.method=='GET':
            ## select contest
            finished = False
            mySelectedContest = db.collection('Matches').document(request.session['choice_match_id_user']).collection('Contests').document(str(request.session['choice_contest_id_user'])).collection('Participants').document(request.session['user_id'])
            
            
            
            ####### USER CONTEST NOT FINISHED 
            if mySelectedContest.get().get('Finished') == False: ## user contest not finished
                print('\n\nUser : ' + str(username) + ', contest is not yet finished \n\n')
                ## call fantasy api
                scoreData = urllib.request.urlopen('https://cricapi.com/api/fantasySummary?apikey=' + str(CRICKET_API) + '&unique_id=' + str(request.session['choice_match_id_user']))
                json_data_new = json.loads(scoreData.read().decode(scoreData.info().get_param('charset') or 'utf-8'))
                print('\ncalled fantasy api\n')
                print('calculate score\n')

                ## Calculate score
                batting_data = json_data_new['data']['batting']
                bowling_data = json_data_new['data']['bowling']
                fielding_data = json_data_new['data']['fielding']

                team = mySelectedContest.get()
                TeamPoints = 0.0
                for x in team.get('Team'):
                    players.append(x)
                    totPoints = 0.0
                    #print('player : ' + str(x['name']) + ',  ID : ' + str(x['pid']))
                    ## Calculate batting score
                    #print('\n\nbatting : ')
                    for innings in batting_data:
                        for score in innings['scores']:
                            #print('ID : ' + str(score['pid']))
                            if str(x['pid']) == str(score['pid']):
                                totPoints = float(BattingScore(score))
                                #print('found and batting score of ' + str(x['name']) + ' : ' + str(totPoints))
                                break

                    #print('\n\nbowling : ')
                    for innings in bowling_data:
                        for score in innings['scores']:
                            #print('ID : ' + str(score['pid']))
                            if str(x['pid']) == str(score['pid']):
                                totPoints += float(BowlingScore(score))
                                #print('found bowling and total score of ' + str(x['name']) + ' : ' + str(totPoints))
                                break

                    #print('\n\nfielding : ')
                    for innings in fielding_data:
                        for score in innings['scores']:
                            #print('ID : ' + str(score['pid']))
                            if str(x['pid']) == str(score['pid']):
                                totPoints += float(FieldingScore(score))
                                #print('found fielding and total score of ' + str(x['name']) + ' : ' + str(totPoints))
                                break

                    ### double captain score and 1.5X vice captain
                    if 'captain' in x:
                        if x['captain']==True:
                            totPoints *= 2
                            #print('ohh captain --> ' + str(x['name']) + ' : ' + str(totPoints))

                    elif 'vicecaptain' in x:
                        if x['vicecaptain']==True:
                            totPoints *= 1.5
                            #print('ohh vice captain --> ' + str(x['name']) + ' : ' + str(totPoints))

                    x['points'] = totPoints
                    TeamPoints += totPoints

                    #if 'winner_team' in 
                team = team.to_dict()
                team['Points'] = TeamPoints
                mySelectedContest.update(team)
                
                ## there is a winner team => finish the contest for the corresponding user
                if 'winner_team' in json_data_new['data']: ## match finished => calculate score for the last time
                    print("\n\nohoh there is winner team, let's finish this : " + str(json_data_new['data']['winner_team']) + '\n\n')
                    finished = {
                        'Finished': True,
                    }
                    mySelectedContest.update(finished)
                    print('\ncontest finished successfully for user' + str(username) + '\n\n')
            

            
            
            
                
            
            ###### Contest is finished for a particular user
            else:
                print('\n\nUser : ' + str(username) + ', contest finished !!!\n\n')
                ### load data for finished match
                team = mySelectedContest.get()
                for x in team.get('Team'):
                    players.append(x)
                team = team.to_dict()
                TeamPoints = team['Points']
                
        return render(request, 'user/status.html', {'players': players, 'TeamPoints': TeamPoints})
    return redirect('fantasycricket:login')
    
################# PAYTM PAYMENT 

def leaderboard(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id'])
        username = user.get().get('Username')
        users = db.collection('Users')
        #print(users)
        #print(users.get())
        #for x in users.get():
        #    print(x.get('Username'))
        
        mySelectedContest = db.collection('Matches').document(request.session['choice_match_id_user']).collection('Contests').document(str(request.session['choice_contest_id_user']))
        
        
        usersToShow = []
        
        #### If the contest is finished , just load the data
        if mySelectedContest.get().get('Finished')==True:
            print('\n\nContest is finished , loading data\n')
            for participant in mySelectedContest.collection('Participants').get():
                print('\n\nparticipant id = ' + str(participant.id))
                for user in users.get():
                    if str(participant.id)==str(user.get('Username')):
                        print('Matched')
                        tempDict = participant.to_dict()
                        tempDict['id'] = str(participant.id)
                        usersToShow.append(tempDict)
        
        
        else:
            print('\n\nContest not finished, calling fantasy api :/\n\n')
            scoreData = urllib.request.urlopen('https://cricapi.com/api/fantasySummary?apikey=' + str(CRICKET_API) + '&unique_id=' + str(request.session['choice_match_id_user']))
            json_data_new = json.loads(scoreData.read().decode(scoreData.info().get_param('charset') or 'utf-8'))
            print('called\n')
            batting_data = json_data_new['data']['batting']
            bowling_data = json_data_new['data']['bowling']
            fielding_data = json_data_new['data']['fielding']

            for participant in mySelectedContest.collection('Participants').get():
                print('\n\nparticipant id = ' + str(participant.id))
                for user in users.get():
                    if str(participant.id)==str(user.get('Username')):
                        print('Matched')

                        tempDict = participant.to_dict()
                        tempDict['id'] = str(participant.id)
                        usersToShow.append(tempDict)

                        team = mySelectedContest.collection('Participants').document(str(user.get('Username'))).get()
                        TeamPoints = 0.0
                        team = team.to_dict()
                        for x in team.get('Team'):
                            totPoints = 0.0
                            for innings in batting_data:
                                for score in innings['scores']:
                                    if str(x['pid']) == str(score['pid']):
                                        totPoints = float(BattingScore(score))
                                        print('bat')
                                        break

                            for innings in bowling_data:
                                for score in innings['scores']:
                                    if str(x['pid']) == str(score['pid']):
                                        totPoints += float(BowlingScore(score))
                                        print('bowl')
                                        break

                            for innings in fielding_data:
                                for score in innings['scores']:
                                    if str(x['pid']) == str(score['pid']):
                                        totPoints += float(FieldingScore(score))
                                        print('field')
                                        break

                            if 'captain' in x:
                                if x['captain']==True:
                                    totPoints *= 2
                                    print('cap')

                            elif 'vicecaptain' in x:
                                if x['vicecaptain']==True:
                                    totPoints *= 1.5
                                    print('v-cap')
                            print(totPoints)
                            x['points'] = totPoints
                            TeamPoints += totPoints

                        #team = team.to_dict()
                        team['Points'] = TeamPoints
                        print('team = ' + str(team))

                        mySelectedContest.collection('Participants').document(str(user.get('Username'))).update(team)
                        

            ### This section is common , should be done for both finished contests and unfinished contests
        
            ## Now after calculating all points of all user, if the match is finished, finish the contest too
            #if db.collection('Matches').document(request.session['choice_match_id_user']).get().get('Finished')==True:
            if 'winner_team' in json_data_new['data']:
                print('\n\noops match finished !!! winner : '+str(json_data_new['data']['winner_team'])+'\n\n')
                ## finish and award the contest
                print('\n\ncalculated all and match is finished !!\n\n')
                finishedAwarded = {
                    'Finished': True,
                    'Awarded': True,
                }
                mySelectedContest.update(finishedAwarded)
                print('\ncontest is made finished and awarded\n\n')
                
                ## Award rankers
                usersToShow = sorted(usersToShow, key=lambda i:(-i['Points'], i['id']))
                count = len(usersToShow)
                maxPoint = usersToShow[0]['Points']
                
                if mySelectedContest.get().get('Type')=='half':
                    ## 50% people gets prize :
                    # 0-10% : 30% of prize
                    # 10-20% : 25% prize
                    # 20-30% : 20% prize
                    # 30-40% : 15% prize
                    # 40-50% : 10% prize
                    counter = 0
                    for x in range(0,5):
                        print('\nhey ' + str(10*x) + ' - '+  str(10*(x+1)) + '\n')
                        start = int((count*(10*x))/100)
                        if start==0:
                            start=x
                            
                        end = int((count*(10*(x+1)))/100)
                        if end==0:
                            end=x+1
                            if end>5:
                                print('\noh no!! break break\n')
                                break
                        counter += 1
                        print('start : '+str(start) + ', end = ' + str(end))
                        for it in range(start, end):
                            
                            per=0
                            if counter==1:
                                per=30
                            elif counter==2:
                                per=25
                            elif counter==3:
                                per=20
                            elif counter==4:
                                per=15
                            elif counter==5:
                                per=10
                                
                            print(str(it) + ' : ' + str(usersToShow[it]['Points']))
                            participant = usersToShow[it]
                            temp_user = db.collection('Users').document(participant['id'])
                            print('toadd : ' + str(float((per*float(mySelectedContest.get().get('TotalPrize')))/100)))
                            credit = {
                                'Winnings': float(temp_user.get().get('Winnings')) + float((per*float(mySelectedContest.get().get('TotalPrize')))/100)
                            }
                            temp_user.update(credit)
                            print('winnings added\n')
                            
                ## for winner gets all
                elif mySelectedContest.get().get('Type')=='winnerAll':
                    print('all')
                    winnercount = 0
                    for x in range(count):
                        if usersToShow[x]['Points']==maxPoint:
                            winnercount += 1
                    prizePerHead = float((mySelectedContest.get().get('TotalPrize'))/winnercount)
                    for x in range(winnercount):
                        participant = usersToShow[x]
                        temp_user = db.collection('Users').document(participant['id'])
                        credit = {
                            'Winnings': float(temp_user.get().get('Winnings')) + float(prizePerHead)
                        }
                        temp_user.update(credit)
                        print('winner all winnings added\n')
                        
        usersToShow = sorted(usersToShow, key=lambda i:(-i['Points'], i['id']))
        i=0
        temp = -100000000000
        for user in usersToShow:
            if temp==float(user['Points']):
                user['rank'] = i
            else:
                temp = float(user['Points'])
                i += 1
                user['rank'] = i
        
        
        return render(request, 'user/leaderboard.html', {'usersToShow': usersToShow})
    return redirect('fantasycricket:login')

def checkout(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        param_dict_paytm = {
            "MID": "MQvxpo66755907856786",
            "ORDER_ID": "fantasycricket"+str(user.get('Username'))+str(random.randint(1,210000)),
            "CUST_ID": "acfff@paytm.com",
            "TXN_AMOUNT": str(request.POST['amount']),
            "CHANNEL_ID": "WEB",
            "INDUSTRY_TYPE_ID": "Retail",
            "WEBSITE": "WEBSTAGING",
            "CALLBACK_URL": "http://localhost:8000/paytm_extra/",
        }
        param_dict_paytm['CHECKSUMHASH'] = Checksum.generate_checksum(param_dict_paytm, MERCHANT_KEY_PAYTM)
        return render(request, 'paytm/checkout.html', {'param_dict_paytm': param_dict_paytm})

@csrf_exempt
def paytm_extra(request):
    print(request.POST)
    if request.POST['STATUS']=='TXN_FAILURE':
        return HttpResponse("PAYMENT FAILED !!")
    else:
        request.session['amount'] = int(float(request.POST['TXNAMOUNT']))
        return redirect('fantasycricket:payment_success')

def payment_success(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        addCash = request.session['amount']
        cash = user.get('Cash') + addCash
        newCash = {
            'Cash': cash,
        }
        db.collection('Users').document(request.session['user_id']).update(newCash)
        #user.get('Cash').set(cash)
        print('\n\nuser cash set successfully\n\n')
        return redirect('fantasycricket:profile')

    
########################### ADMIN SECTION
def superProfile(request):
    if 'user_id' in request.session:
        user = db.collection('Users').document(request.session['user_id']).get()
        admin = isAdmin(user)
        if admin:
            users = []
            
            if request.method=='POST':
                if 'editBtn' in request.POST:
                    print(request.POST['username'])
                    userToEdit = db.collection('Users').document(request.POST['username']).get()
                    userToEdit = userToEdit.to_dict()
                    print(str(userToEdit))
                    userToEdit['Name'] = request.POST['name']
                    userToEdit['Email'] = request.POST['email']
                    userToEdit['Cash'] = float(request.POST['cash'])
                    userToEdit['xp'] = float(request.POST['xp'])
                    userToEdit['Winnings'] = float(request.POST['winnings'])
                    db.collection('Users').document(request.POST['username']).update(userToEdit)
                    print('updated successfully')
            
            for x in db.collection('Users').get():
                #print(str(x.to_dict()))
                users.append(x.to_dict())                
            return render(request, 'admin/controlProfile.html', {'users': users})
        return redirect('fantasycricket:profile')
    return redirect('fantasycricket:login')
