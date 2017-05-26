import {Injectable} from 'injection-js';
import * as qwest from 'qwest';
import {StompClient} from '@c1/stomp-client';
import {Observable, BehaviorSubject} from 'rxjs';
import {plainToClass} from 'class-transformer';

import {Observable} from './rxjs';
import {Single} from './rxjs';


@Injectable()
export class AccountsRestControllerService {
//-----------------Constructor
     constructor( private stompClient: StompClient ) {}

//-----------------Stomp Methods

public accounts(): Observable<Array<Account>> {
    return this.stompClient.topic('/queue/accounts')
       .map(x => plainToClass(Account, x));
}

//-----------------Rest Methods

public addAccount( body : Account) : Single<boolean> {
   let o = new BehaviorSubject<boolean>();
   qwest.post( '/api/accounts/', body )
       .then((xhr, response) => {
            let x = plainToClass(boolean, response);
            o.next(x);
        })
       .catch((e, xhr, response)  => {
            o.error(e);
        });
   return o.asObservable();
}
}
